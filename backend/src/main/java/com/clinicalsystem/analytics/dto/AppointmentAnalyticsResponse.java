package com.clinicalsystem.analytics.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AppointmentAnalyticsResponse {

    private LocalDate from;
    private LocalDate to;
    private long totalCount;
    private long completedCount;
    private long cancelledCount;
    private long noShowCount;
    private BigDecimal totalRevenue;
    private BigDecimal averageConsultationFee;

    /** Day-by-day appointment counts: { "2025-04-01": 5, "2025-04-02": 8 } */
    private Map<String, Long> dailyCounts;

    /** Specialty breakdown: { "Cardiology": 12, "General": 20 } */
    private Map<String, Long> bySpecialty;

    /** Type breakdown: { "ONLINE": 30, "IN_PERSON": 10 } */
    private Map<String, Long> byType;

    /** Top 5 doctors by appointment count */
    private List<DoctorStat> topDoctors;

    @Getter
    @Builder
    public static class DoctorStat {
        private String doctorId;
        private String doctorName;
        private long appointmentCount;
        private BigDecimal revenue;
        private double averageRating;
    }
}
