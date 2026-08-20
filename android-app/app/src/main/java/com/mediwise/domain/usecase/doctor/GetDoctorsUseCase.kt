package com.mediwise.domain.usecase.doctor

import com.mediwise.core.result.Result
import com.mediwise.domain.model.Doctor
import com.mediwise.domain.repository.DoctorRepository
import javax.inject.Inject

class GetDoctorsUseCase @Inject constructor(private val repo: DoctorRepository) {
    suspend operator fun invoke(search: String? = null, specialty: String? = null,
                                sortBy: String = "rating", page: Int = 0, size: Int = 20): Result<List<Doctor>> =
        repo.getDoctors(specialty, search, sortBy, page, size)
}
