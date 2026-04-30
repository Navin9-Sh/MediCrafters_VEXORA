package com.clinicalsystem.appointment.controller;

import com.clinicalsystem.appointment.dto.AppointmentResponse;
import com.clinicalsystem.appointment.dto.BookAppointmentRequest;
import com.clinicalsystem.appointment.dto.CancelRequest;
import com.clinicalsystem.appointment.service.AppointmentService;
import com.clinicalsystem.auth.model.User;
import com.clinicalsystem.common.response.ApiResponse;
import com.clinicalsystem.common.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Book, view and cancel appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Book a new appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> book(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BookAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(appointmentService.bookAppointment(user, request)));
    }

    @GetMapping
    @Operation(summary = "Get my appointments (filter by status)")
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentResponse>>> getMyAppointments(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(appointmentService.getMyAppointments(user, status, page, size))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment detail")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentById(id, user)));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancel(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @RequestBody CancelRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.cancelAppointment(id, user, request)));
    }
}
