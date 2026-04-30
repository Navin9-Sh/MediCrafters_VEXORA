package com.clinicalsystem.ai.repository;

import com.clinicalsystem.ai.model.AiReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiReportRepository extends MongoRepository<AiReport, String> {

    List<AiReport> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    Optional<AiReport> findTopByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
