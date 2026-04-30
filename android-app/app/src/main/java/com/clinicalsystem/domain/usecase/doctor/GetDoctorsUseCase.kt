package com.clinicalsystem.domain.usecase.doctor

import com.clinicalsystem.core.result.Result
import com.clinicalsystem.domain.model.Doctor
import com.clinicalsystem.domain.repository.DoctorRepository
import javax.inject.Inject

class GetDoctorsUseCase @Inject constructor(private val repo: DoctorRepository) {
    suspend operator fun invoke(search: String? = null, specialty: String? = null,
                                sortBy: String = "rating", page: Int = 0, size: Int = 20): Result<List<Doctor>> =
        repo.getDoctors(specialty, search, sortBy, page, size)
}
