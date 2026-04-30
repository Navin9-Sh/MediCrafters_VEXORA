package com.clinicalsystem.schedule.dto;

import com.clinicalsystem.schedule.model.TimeSlot;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data @Builder
public class SlotResponse {
    private UUID id;
    private UUID doctorId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private TimeSlot.SlotStatus status;

    public static SlotResponse from(TimeSlot s) {
        return SlotResponse.builder()
                .id(s.getId())
                .doctorId(s.getDoctorId())
                .slotDate(s.getSlotDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .status(s.getStatus())
                .build();
    }
}
