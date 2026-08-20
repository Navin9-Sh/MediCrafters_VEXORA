package com.mediwise.schedule.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "time_slots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "slot_date", "start_time"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private SlotStatus status = SlotStatus.AVAILABLE;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "locked_by")
    private UUID lockedBy;

    public enum SlotStatus {
        AVAILABLE, LOCKED, BOOKED, CANCELLED
    }
}
