package com.mediwise.appointment.dto;

import com.mediwise.appointment.model.Appointment;
import com.mediwise.schedule.model.TimeSlot;
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
    private String doctorName;
    private String doctorSpecialty;
    private String doctorProfileImage;
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
        return from(a, slot, null, null, null);
    }

    public static AppointmentResponse from(Appointment a, TimeSlot slot, String doctorName, String doctorSpecialty, String doctorProfileImage) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatientId())
                .doctorId(a.getDoctorId())
                .doctorName(doctorName)
                .doctorSpecialty(doctorSpecialty)
                .doctorProfileImage(doctorProfileImage)
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
