package com.clinicalsystem.appointment.dto;

import com.clinicalsystem.appointment.model.Appointment;
import com.clinicalsystem.schedule.model.TimeSlot;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data @Builder
public class AppointmentResponse {
    private UUID id;
    private UUID patientId;
    private UUID doctorId;
    private UUID slotId;
    private LocalDate slotDate;
    private LocalTime slotStartTime;
    private LocalTime slotEndTime;
    private Appointment.AppointmentStatus status;
    private Appointment.AppointmentType type;
    private String chiefComplaint;
    private String notes;
    private String cancelReason;
    private Instant createdAt;
    private Instant updatedAt;

    public static AppointmentResponse from(Appointment a, TimeSlot slot) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatientId())
                .doctorId(a.getDoctorId())
                .slotId(a.getSlotId())
                .slotDate(slot != null ? slot.getSlotDate() : null)
                .slotStartTime(slot != null ? slot.getStartTime() : null)
                .slotEndTime(slot != null ? slot.getEndTime() : null)
                .status(a.getStatus())
                .type(a.getType())
                .chiefComplaint(a.getChiefComplaint())
                .notes(a.getNotes())
                .cancelReason(a.getCancelReason())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
