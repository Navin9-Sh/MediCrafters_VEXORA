package com.clinicalsystem.doctor.service;

import com.clinicalsystem.doctor.model.Doctor;
import org.springframework.data.jpa.domain.Specification;

public class DoctorSpecification {

    public static Specification<Doctor> hasSpecialty(String specialty) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("specialty")),
                        "%" + specialty.toLowerCase() + "%");
    }

    public static Specification<Doctor> isAvailable() {
        return (root, query, cb) -> cb.isTrue(root.get("available"));
    }

    public static Specification<Doctor> isVerified() {
        return (root, query, cb) -> cb.isTrue(root.get("verified"));
    }
}
