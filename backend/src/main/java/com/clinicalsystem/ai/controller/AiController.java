package com.clinicalsystem.ai.controller;

import com.clinicalsystem.ai.dto.AiReportResponse;
import com.clinicalsystem.ai.dto.SymptomLogRequest;
import com.clinicalsystem.ai.service.AiService;
import com.clinicalsystem.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI / Clinical Decision", description = "AI-powered symptom triage and clinical report endpoints")
public class AiController {

    private final AiService aiService;

    @PostMapping("/symptom-log")
    @Operation(summary = "Log patient symptoms and receive AI triage result")
    public ResponseEntity<ApiResponse<AiReportResponse>> logSymptoms(
            @Valid @RequestBody SymptomLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success(aiService.analyzeSymptoms(request)));
    }

    @GetMapping("/reports/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @Operation(summary = "Get all AI reports for a patient (Doctor/Admin only)")
    public ResponseEntity<ApiResponse<List<AiReportResponse>>> getReports(
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(ApiResponse.success(aiService.getReportsForPatient(patientId)));
    }

    @GetMapping("/reports/{patientId}/latest")
    @Operation(summary = "Get latest AI report for a patient")
    public ResponseEntity<ApiResponse<AiReportResponse>> getLatestReport(
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(ApiResponse.success(aiService.getLatestReport(patientId)));
    }
}
