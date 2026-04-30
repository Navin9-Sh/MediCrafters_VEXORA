package com.clinicalsystem.doctor.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "doctor_favorites")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DoctorFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
