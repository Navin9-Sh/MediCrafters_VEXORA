package com.clinicalsystem.schedule.repository;

import com.clinicalsystem.schedule.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    @Query("SELECT s FROM TimeSlot s WHERE s.doctorId = :doctorId " +
           "AND s.slotDate = :date AND s.status = 'AVAILABLE' " +
           "ORDER BY s.startTime ASC")
    List<TimeSlot> findAvailableSlots(@Param("doctorId") UUID doctorId,
                                       @Param("date") LocalDate date);

    @Modifying
    @Query("UPDATE TimeSlot s SET s.status = 'AVAILABLE', s.lockedBy = null, " +
           "s.lockedUntil = null WHERE s.lockedUntil < CURRENT_TIMESTAMP " +
           "AND s.status = 'LOCKED'")
    void releaseExpiredLocks();
}
