package com.clinicalsystem.analytics.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardStatsResponse {

    private long totalAppointments;
    private long pendingAppointments;
    private long confirmedAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long totalPatients;
    private long totalDoctors;
    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    private double averageRating;
    private long totalReviews;
    private long unreadNotifications;
}
