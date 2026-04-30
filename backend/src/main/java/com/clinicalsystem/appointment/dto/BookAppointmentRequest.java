package com.clinicalsystem.appointment.dto;

import com.clinicalsystem.appointment.model.Appointment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookAppointmentRequest {

    @NotNull(message = "Slot ID is required")
    private UUID slotId;

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    private Appointment.AppointmentType type = Appointment.AppointmentType.ONLINE;

    private String chiefComplaint;
}
