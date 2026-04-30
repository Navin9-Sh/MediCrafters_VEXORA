package com.clinicalsystem.data.repository

import com.clinicalsystem.core.network.safeApiCall
import com.clinicalsystem.core.result.Result
import com.clinicalsystem.data.local.dao.AppointmentDao
import com.clinicalsystem.data.local.entity.toEntity
import com.clinicalsystem.data.remote.api.AppointmentApi
import com.clinicalsystem.data.remote.dto.*
import com.clinicalsystem.domain.model.Appointment
import com.clinicalsystem.domain.repository.AppointmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepositoryImpl @Inject constructor(
    private val api: AppointmentApi,
    private val appointmentDao: AppointmentDao
) : AppointmentRepository {
    
    override suspend fun getMyAppointments(status: String?, page: Int, size: Int): Result<List<Appointment>> {
        val result = safeApiCall {
            val response = api.getMyAppointments(status, page, size)
            response.data?.content?.map { it.toDomain() } ?: emptyList()
        }
        
        if (result is Result.Success) {
            appointmentDao.insertAppointments(result.data.map { it.toEntity() })
        }
        return result
    }

    override suspend fun getAppointmentById(id: String): Result<Appointment> {
        val cached = appointmentDao.getAppointmentById(id)
        if (cached != null) {
            return Result.Success(cached.toDomain())
        }
        
        return safeApiCall {
            val response = api.getAppointmentById(id)
            val appt = response.data!!.toDomain()
            appointmentDao.insertAppointment(appt.toEntity())
            appt
        }
    }

    override suspend fun bookAppointment(doctorId: String, date: String, time: String, type: String): Result<Appointment> {
        return safeApiCall {
            val request = BookAppointmentRequestDto(slotId = "", doctorId = doctorId, type = type)
            val response = api.bookAppointment(request)
            val newAppt = response.data!!.toDomain()
            appointmentDao.insertAppointment(newAppt.toEntity())
            newAppt
        }
    }

    override suspend fun cancelAppointment(id: String, reason: String?): Result<Appointment> {
        return safeApiCall {
            val request = CancelRequestDto(reason = reason)
            val response = api.cancelAppointment(id, request)
            val cancelledAppt = response.data!!.toDomain()
            appointmentDao.insertAppointment(cancelledAppt.toEntity())
            cancelledAppt
        }
    }
}
