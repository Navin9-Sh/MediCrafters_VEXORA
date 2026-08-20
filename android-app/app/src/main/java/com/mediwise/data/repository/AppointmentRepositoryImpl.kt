package com.mediwise.data.repository

import com.mediwise.core.network.safeApiCall
import com.mediwise.core.result.Result
import com.mediwise.data.local.dao.AppointmentDao
import com.mediwise.data.local.entity.toEntity
import com.mediwise.data.remote.api.AppointmentApi
import com.mediwise.data.remote.dto.*
import com.mediwise.domain.model.Appointment
import com.mediwise.domain.repository.AppointmentRepository
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
