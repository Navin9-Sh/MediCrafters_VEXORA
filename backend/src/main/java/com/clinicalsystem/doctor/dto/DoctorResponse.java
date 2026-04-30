package com.clinicalsystem.doctor.dto;

import com.clinicalsystem.doctor.model.Doctor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Data @Builder
public class DoctorResponse {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String bio;
    private String specialty;
    private Set<String> specialties;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String profileImage;
    private BigDecimal avgRating;
    private Integer totalReviews;
    private boolean available;
    private boolean verified;

    public static DoctorResponse from(Doctor d) {
        return DoctorResponse.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .fullName(d.getFullName())
                .bio(d.getBio())
                .specialty(d.getSpecialty())
                .specialties(d.getSpecialties())
                .experienceYears(d.getExperienceYears())
                .consultationFee(d.getConsultationFee())
                .profileImage(d.getProfileImage())
                .avgRating(d.getAvgRating())
                .totalReviews(d.getTotalReviews())
                .available(d.isAvailable())
                .verified(d.isVerified())
                .build();
    }
}
