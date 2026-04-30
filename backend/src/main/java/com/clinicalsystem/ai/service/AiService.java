package com.clinicalsystem.ai.service;

import com.clinicalsystem.ai.dto.AiReportResponse;
import com.clinicalsystem.ai.dto.SymptomLogRequest;
import com.clinicalsystem.ai.model.AiReport;
import com.clinicalsystem.ai.model.SymptomLog;
import com.clinicalsystem.ai.repository.AiReportRepository;
import com.clinicalsystem.ai.repository.SymptomLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI Service — currently returns stub responses.
 * When the Python ML FastAPI service is deployed, replace stubAnalyze()
 * with a WebClient call to the ML service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final AiReportRepository aiReportRepository;
    private final SymptomLogRepository symptomLogRepository;

    public AiReportResponse analyzeSymptoms(SymptomLogRequest request) {
        // 1. Persist the symptom log
        SymptomLog log = new SymptomLog();
        log.setPatientId(request.getPatientId());
        log.setAppointmentId(request.getAppointmentId());
        log.setSymptoms(request.getSymptoms());
        log.setSeverity(request.getSeverity());
        log.setNotes(request.getNotes());
        log.setLoggedAt(Instant.now());
        symptomLogRepository.save(log);

        // 2. Generate AI analysis (stub — swap with WebClient to Python service)
        AiReport report = stubAnalyze(request);
        AiReport saved = aiReportRepository.save(report);

        return toResponse(saved);
    }

    public List<AiReportResponse> getReportsForPatient(UUID patientId) {
        return aiReportRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public AiReportResponse getLatestReport(UUID patientId) {
        return aiReportRepository.findTopByPatientIdOrderByCreatedAtDesc(patientId)
                .map(this::toResponse)
                .orElse(null);
    }

    // ─── Stub implementation ────────────────────────────────────────────────────
    // Replace with: webClient.post().uri(mlServiceUrl + "/predict/symptom-triage")...

    private AiReport stubAnalyze(SymptomLogRequest req) {
        // Simple heuristic based on severity
        int urgency = switch (req.getSeverity() != null ? req.getSeverity().toUpperCase() : "LOW") {
            case "CRITICAL" -> 90;
            case "HIGH"     -> 70;
            case "MODERATE" -> 45;
            default         -> 20;
        };

        String specialty = urgency > 65 ? "Emergency Medicine" :
                           urgency > 40 ? "General Medicine" : "General Practice";

        AiReport report = new AiReport();
        report.setPatientId(req.getPatientId());
        report.setAppointmentId(req.getAppointmentId());
        report.setModelName("symptom_triage_v1_stub");
        report.setModelVersion("1.0.0-stub");
        report.setUrgencyScore(urgency);
        report.setSuggestedSpecialty(specialty);
        report.setConfidence(0.72); // stub confidence
        report.setRecommendation("Based on reported symptoms, we recommend consulting a " + specialty + " at the earliest.");
        report.setRiskFactors(req.getSymptoms() != null && !req.getSymptoms().isEmpty()
                ? req.getSymptoms().subList(0, Math.min(3, req.getSymptoms().size()))
                : List.of());
        report.setCreatedAt(Instant.now());
        return report;
    }

    private AiReportResponse toResponse(AiReport r) {
        return AiReportResponse.builder()
                .id(r.getId())
                .patientId(r.getPatientId())
                .appointmentId(r.getAppointmentId())
                .modelName(r.getModelName())
                .modelVersion(r.getModelVersion())
                .urgencyScore(r.getUrgencyScore())
                .suggestedSpecialty(r.getSuggestedSpecialty())
                .confidence(r.getConfidence())
                .recommendation(r.getRecommendation())
                .riskFactors(r.getRiskFactors())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
