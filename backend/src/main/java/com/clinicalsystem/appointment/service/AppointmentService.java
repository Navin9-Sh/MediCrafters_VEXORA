package com.clinicalsystem.appointment.service;

import com.clinicalsystem.appointment.dto.BookAppointmentRequest;
import com.clinicalsystem.appointment.dto.AppointmentResponse;
import com.clinicalsystem.appointment.dto.CancelRequest;
import com.clinicalsystem.appointment.event.AppointmentBookedEvent;
import com.clinicalsystem.appointment.model.Appointment;
import com.clinicalsystem.appointment.repository.AppointmentRepository;
import com.clinicalsystem.auth.model.User;
import com.clinicalsystem.common.exception.BusinessException;
import com.clinicalsystem.common.exception.ResourceNotFoundException;
import com.clinicalsystem.common.exception.SlotConflictException;
import com.clinicalsystem.profile.model.PatientProfile;
import com.clinicalsystem.profile.repository.PatientProfileRepository;
import com.clinicalsystem.schedule.model.TimeSlot;
import com.clinicalsystem.schedule.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository slotRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @CacheEvict(value = "slots", allEntries = true)
    public AppointmentResponse bookAppointment(User user, BookAppointmentRequest request) {
        PatientProfile patient = patientProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("PROFILE_REQUIRED",
                        "Complete your profile before booking an appointment."));

        TimeSlot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot", request.getSlotId().toString()));

        // Verify the lock belongs to this user
        if (slot.getStatus() != TimeSlot.SlotStatus.LOCKED ||
                !user.getId().equals(slot.getLockedBy())) {
            throw new SlotConflictException("Slot is not reserved for you. Please re-select.");
        }

        // Mark slot as BOOKED
        slot.setStatus(TimeSlot.SlotStatus.BOOKED);
        slot.setLockedBy(null);
        slot.setLockedUntil(null);
        slotRepository.save(slot);

        Appointment appointment = Appointment.builder()
                .patientId(patient.getId())
                .doctorId(request.getDoctorId())
                .slotId(slot.getId())
                .type(request.getType())
                .chiefComplaint(request.getChiefComplaint())
                .status(Appointment.AppointmentStatus.PENDING)
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Appointment {} booked by patient {}", appointment.getId(), patient.getId());

        // Publish domain event → triggers push notifications asynchronously
        eventPublisher.publishEvent(new AppointmentBookedEvent(this, appointment));

        return AppointmentResponse.from(appointment, slot);
    }

    public Page<AppointmentResponse> getMyAppointments(User user, String status, int page, int size) {
        PatientProfile patient = patientProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", user.getId().toString()));

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (status != null && !status.isBlank()) {
            Appointment.AppointmentStatus appointmentStatus =
                    Appointment.AppointmentStatus.valueOf(status.toUpperCase());
            return appointmentRepository
                    .findByPatientIdAndStatusOrderByCreatedAtDesc(patient.getId(), appointmentStatus, pageable)
                    .map(a -> {
                        TimeSlot slot = slotRepository.findById(a.getSlotId()).orElse(null);
                        return AppointmentResponse.from(a, slot);
                    });
        }

        return appointmentRepository
                .findByPatientIdOrderByCreatedAtDesc(patient.getId(), pageable)
                .map(a -> {
                    TimeSlot slot = slotRepository.findById(a.getSlotId()).orElse(null);
                    return AppointmentResponse.from(a, slot);
                });
    }

    public AppointmentResponse getAppointmentById(UUID id, User user) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id.toString()));
        TimeSlot slot = slotRepository.findById(appointment.getSlotId()).orElse(null);
        return AppointmentResponse.from(appointment, slot);
    }

    @Transactional
    @CacheEvict(value = "slots", allEntries = true)
    public AppointmentResponse cancelAppointment(UUID id, User user, CancelRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id.toString()));

        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED ||
                appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            throw new BusinessException("CANNOT_CANCEL",
                    "This appointment cannot be cancelled in its current status.");
        }

        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointment.setCancelledBy(user.getId());
        appointment.setCancelReason(request.getReason());
        appointmentRepository.save(appointment);

        // Release the slot
        slotRepository.findById(appointment.getSlotId()).ifPresent(slot -> {
            slot.setStatus(TimeSlot.SlotStatus.AVAILABLE);
            slotRepository.save(slot);
        });

        log.info("Appointment {} cancelled by user {}", id, user.getId());
        TimeSlot slot = slotRepository.findById(appointment.getSlotId()).orElse(null);
        return AppointmentResponse.from(appointment, slot);
    }
}
