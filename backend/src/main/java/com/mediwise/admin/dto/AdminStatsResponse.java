package com.mediwise.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Platform-wide statistics returned by GET /api/v1/admin/stats
 *
 * Intended for an admin dashboard showing high-level health metrics.
 * All counts are computed via DB aggregate queries — not in-memory.
 *
 * INTERVIEW NOTE: We use repository count queries here, not a
 * separate analytics database, because the volumes are small enough
 * that a few COUNT(*) queries are cheap. At scale, these would be
 * pre-computed by a scheduled job and stored in Redis.
 */
@Data
@Builder
public class AdminStatsResponse {

    // ── User metrics ──────────────────────────────────────────────────────
    private long totalUsers;
    private long totalPatients;
    private long totalDoctors;
    private long pendingDoctorVerifications;  // doctors waiting for admin approval
    private long activeUsers;                 // users with active=true

    // ── Appointment metrics ───────────────────────────────────────────────
    private long totalAppointments;
    private long pendingAppointments;
    private long confirmedAppointments;
    private long completedAppointments;
    private long cancelledAppointments;

    // ── Revenue metrics ───────────────────────────────────────────────────
    /** Total revenue collected = sum of all successful payment amounts */
    private BigDecimal totalRevenue;

    /** Number of successful payments processed */
    private long successfulPayments;
}
