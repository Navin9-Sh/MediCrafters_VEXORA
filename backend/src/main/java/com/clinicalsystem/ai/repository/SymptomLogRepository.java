package com.clinicalsystem.ai.repository;

import com.clinicalsystem.ai.model.SymptomLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SymptomLogRepository extends MongoRepository<SymptomLog, String> {

    List<SymptomLog> findByPatientIdOrderByLoggedAtDesc(UUID patientId);
}
