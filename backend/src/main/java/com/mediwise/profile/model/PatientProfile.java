package com.mediwise.profile.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patient_profiles")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "blood_type", length = 5)
    private String bloodType;

    @Column(length = 10)
    private String gender;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String address;

    @Column(name = "emergency_contact", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String emergencyContact;
}
