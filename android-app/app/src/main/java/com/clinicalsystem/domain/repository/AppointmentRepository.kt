package com.clinicalsystem.domain.repository

import com.clinicalsystem.core.result.Result
import com.clinicalsystem.domain.model.Appointment

interface AppointmentRepository {
    suspend fun getMyAppointments(status: String?, page: Int = 0, size: Int = 20): Result<List<Appointment>>
    suspend fun getAppointmentById(id: String): Result<Appointment>
    suspend fun bookAppointment(doctorId: String, date: String, time: String, type: String): Result<Appointment>
    suspend fun cancelAppointment(id: String, reason: String? = null): Result<Appointment>
}
