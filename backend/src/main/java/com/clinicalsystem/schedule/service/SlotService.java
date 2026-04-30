package com.clinicalsystem.schedule.service;

import com.clinicalsystem.auth.model.User;
import com.clinicalsystem.common.exception.BusinessException;
import com.clinicalsystem.common.exception.ResourceNotFoundException;
import com.clinicalsystem.common.exception.SlotConflictException;
import com.clinicalsystem.schedule.dto.SlotLockResponse;
import com.clinicalsystem.schedule.dto.SlotResponse;
import com.clinicalsystem.schedule.model.TimeSlot;
import com.clinicalsystem.schedule.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlotService {

    private final TimeSlotRepository slotRepository;
    
    @Autowired(required = false)
    private RedissonClient redissonClient;

    private static final long LOCK_TTL_MINUTES = 5;

    @Cacheable(value = "slots", key = "#doctorId + '_' + #date")
    public List<SlotResponse> getAvailableSlots(UUID doctorId, LocalDate date) {
        return slotRepository.findAvailableSlots(doctorId, date)
                .stream()
                .map(SlotResponse::from)
                .toList();
    }

    @Transactional
    public SlotLockResponse lockSlot(UUID slotId, User user) {
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot", slotId.toString()));

        if (redissonClient == null) {
            if (slot.getStatus() != TimeSlot.SlotStatus.AVAILABLE) {
                throw new SlotConflictException();
            }
            Instant expiresAt = Instant.now().plusSeconds(LOCK_TTL_MINUTES * 60);
            slot.setStatus(TimeSlot.SlotStatus.LOCKED);
            slot.setLockedBy(user.getId());
            slot.setLockedUntil(expiresAt);
            slotRepository.save(slot);

            log.info("Slot {} locked by user {} (without distributed lock)", slotId, user.getId());
            return SlotLockResponse.builder()
                    .slotId(slotId)
                    .locked(true)
                    .expiresAt(expiresAt)
                    .ttlMinutes(LOCK_TTL_MINUTES)
                    .build();
        }

        // Use Redisson distributed lock to prevent race conditions
        RLock rLock = redissonClient.getLock("slot_lock:" + slotId);
        try {
            boolean acquired = rLock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new SlotConflictException();
            }

            // Re-fetch inside lock
            slot = slotRepository.findById(slotId).orElseThrow();
            if (slot.getStatus() != TimeSlot.SlotStatus.AVAILABLE) {
                throw new SlotConflictException();
            }

            Instant expiresAt = Instant.now().plusSeconds(LOCK_TTL_MINUTES * 60);
            slot.setStatus(TimeSlot.SlotStatus.LOCKED);
            slot.setLockedBy(user.getId());
            slot.setLockedUntil(expiresAt);
            slotRepository.save(slot);

            log.info("Slot {} locked by user {}", slotId, user.getId());
            return SlotLockResponse.builder()
                    .slotId(slotId)
                    .locked(true)
                    .expiresAt(expiresAt)
                    .ttlMinutes(LOCK_TTL_MINUTES)
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("LOCK_FAILED", "Could not acquire slot lock. Try again.");
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    @Transactional
    public void releaseSlot(UUID slotId, User user) {
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot", slotId.toString()));

        if (!user.getId().equals(slot.getLockedBy())) {
            throw new BusinessException("LOCK_OWNER_MISMATCH", "You do not hold the lock for this slot.");
        }

        slot.setStatus(TimeSlot.SlotStatus.AVAILABLE);
        slot.setLockedBy(null);
        slot.setLockedUntil(null);
        slotRepository.save(slot);
    }

    @Scheduled(fixedDelay = 60_000) // every 1 minute
    @Transactional
    public void cleanExpiredLocks() {
        slotRepository.releaseExpiredLocks();
    }
}
