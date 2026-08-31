package com.mediwise.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediwise.admin.dto.AdminStatsResponse;
import com.mediwise.admin.dto.DoctorApprovalRequest;
import com.mediwise.admin.dto.UserStatusUpdateRequest;
import com.mediwise.admin.dto.UserSummaryResponse;
import com.mediwise.admin.service.AdminService;
import com.mediwise.appointment.dto.AppointmentResponse;
import com.mediwise.appointment.model.Appointment;
import com.mediwise.auth.model.User;
import com.mediwise.doctor.dto.DoctorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private UUID sampleUserId;
    private UUID sampleDoctorId;
    private UserSummaryResponse sampleUserSummary;
    private DoctorResponse sampleDoctorResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        sampleUserId = UUID.randomUUID();
        sampleDoctorId = UUID.randomUUID();

        sampleUserSummary = UserSummaryResponse.builder()
                .id(sampleUserId)
                .email("user@mediwise.com")
                .fullName("Test Patient")
                .role(User.Role.PATIENT)
                .active(true)
                .createdAt(Instant.now())
                .build();

        sampleDoctorResponse = DoctorResponse.builder()
                .id(sampleDoctorId)
                .userId(sampleUserId)
                .fullName("Dr. Sarah")
                .specialty("Cardiology")
                .consultationFee(BigDecimal.valueOf(800))
                .verified(false)
                .available(true)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/admin/stats should return aggregated platform statistics")
    void testGetStats() throws Exception {
        AdminStatsResponse stats = AdminStatsResponse.builder()
                .totalUsers(120)
                .totalPatients(100)
                .totalDoctors(20)
                .activeUsers(115)
                .pendingDoctorVerifications(5)
                .totalAppointments(350)
                .confirmedAppointments(200)
                .completedAppointments(100)
                .cancelledAppointments(50)
                .totalRevenue(new BigDecimal("150000.00"))
                .successfulPayments(300)
                .build();

        when(adminService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/admin/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").value(120))
                .andExpect(jsonPath("$.data.totalDoctors").value(20))
                .andExpect(jsonPath("$.data.totalRevenue").value(150000.00));
    }

    @Test
    @DisplayName("GET /api/v1/admin/users should return paged users")
    void testGetUsers() throws Exception {
        when(adminService.getUsers(eq(User.Role.PATIENT), isNull(), eq(0), eq(10)))
                .thenReturn(new PageImpl<>(List.of(sampleUserSummary)));

        mockMvc.perform(get("/api/v1/admin/users?role=PATIENT&page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].email").value("user@mediwise.com"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/users/{id}/status should update status")
    void testUpdateUserStatus() throws Exception {
        UserStatusUpdateRequest request = UserStatusUpdateRequest.builder()
                .active(false)
                .build();

        sampleUserSummary.setActive(false);
        when(adminService.updateUserStatus(eq(sampleUserId), eq(false))).thenReturn(sampleUserSummary);

        mockMvc.perform(patch("/api/v1/admin/users/" + sampleUserId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/doctors/{id}/verify should verify doctor")
    void testVerifyDoctor() throws Exception {
        sampleDoctorResponse.setVerified(true);
        when(adminService.verifyDoctor(eq(sampleDoctorId), eq(true))).thenReturn(sampleDoctorResponse);

        mockMvc.perform(patch("/api/v1/admin/doctors/" + sampleDoctorId + "/verify")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verified").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/doctors/{id}/reject should reject doctor")
    void testRejectDoctor() throws Exception {
        DoctorApprovalRequest request = DoctorApprovalRequest.builder()
                .reason("License could not be verified")
                .build();

        mockMvc.perform(patch("/api/v1/admin/doctors/" + sampleDoctorId + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(adminService).rejectDoctor(eq(sampleDoctorId), eq("License could not be verified"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/appointments should return paged appointments for platform oversight")
    void testGetAllAppointments() throws Exception {
        AppointmentResponse sampleAppointment = AppointmentResponse.builder()
                .id(UUID.randomUUID())
                .patientId(sampleUserId)
                .doctorId(sampleDoctorId)
                .doctorName("Dr. Sarah")
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .build();

        when(adminService.getAllAppointments(eq("CONFIRMED"), eq(0), eq(10)))
                .thenReturn(new PageImpl<>(List.of(sampleAppointment)));

        mockMvc.perform(get("/api/v1/admin/appointments?status=CONFIRMED&page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].doctorName").value("Dr. Sarah"))
                .andExpect(jsonPath("$.data.content[0].status").value("CONFIRMED"));
    }
}
