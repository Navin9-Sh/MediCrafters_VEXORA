package com.mediwise.doctor.repository;

import com.mediwise.doctor.model.DoctorFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorFavoriteRepository extends JpaRepository<DoctorFavorite, UUID> {

    @Query("SELECT f FROM DoctorFavorite f WHERE f.patientId = :patientId")
    Page<DoctorFavorite> findByPatientId(@Param("patientId") UUID patientId, Pageable pageable);

    Optional<DoctorFavorite> findByPatientIdAndDoctorId(UUID patientId, UUID doctorId);

    boolean existsByPatientIdAndDoctorId(UUID patientId, UUID doctorId);

    void deleteByPatientIdAndDoctorId(UUID patientId, UUID doctorId);
}
