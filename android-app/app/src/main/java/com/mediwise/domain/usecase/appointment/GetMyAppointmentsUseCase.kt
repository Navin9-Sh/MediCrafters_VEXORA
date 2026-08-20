package com.mediwise.domain.usecase.appointment

import com.mediwise.core.result.Result
import com.mediwise.domain.model.Appointment
import com.mediwise.domain.repository.AppointmentRepository
import javax.inject.Inject

class GetMyAppointmentsUseCase @Inject constructor(private val repo: AppointmentRepository) {
    suspend operator fun invoke(status: String? = null, page: Int = 0, size: Int = 20): Result<List<Appointment>> =
        repo.getMyAppointments(status, page, size)
}
