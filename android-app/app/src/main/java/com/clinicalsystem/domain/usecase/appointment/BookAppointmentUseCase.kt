package com.clinicalsystem.domain.usecase.appointment

import com.clinicalsystem.core.result.Result
import com.clinicalsystem.domain.model.Appointment
import com.clinicalsystem.domain.repository.AppointmentRepository
import javax.inject.Inject

class BookAppointmentUseCase @Inject constructor(private val repo: AppointmentRepository) {
    suspend operator fun invoke(doctorId: String, date: String, time: String, type: String): Result<Appointment> =
        repo.bookAppointment(doctorId, date, time, type)
}
