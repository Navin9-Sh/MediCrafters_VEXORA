package com.clinicalsystem.data.repository

import com.clinicalsystem.core.network.safeApiCall
import com.clinicalsystem.core.result.Result
import com.clinicalsystem.data.local.dao.DoctorDao
import com.clinicalsystem.data.local.entity.toEntity
import com.clinicalsystem.data.remote.api.DoctorApi
import com.clinicalsystem.data.remote.dto.*
import com.clinicalsystem.domain.model.Doctor
import com.clinicalsystem.domain.repository.DoctorRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoctorRepositoryImpl @Inject constructor(
    private val api: DoctorApi,
    private val doctorDao: DoctorDao
) : DoctorRepository {
    override suspend fun getDoctors(specialty: String?, search: String?, sortBy: String, page: Int, size: Int): Result<List<Doctor>> {
        val result = safeApiCall {
            val response = api.getDoctors(
                specialty = if (specialty == "All") null else specialty,
                search = search?.takeIf { it.isNotBlank() },
                sortBy = sortBy,
                page = page,
                size = size
            )
            response.data?.content?.map { it.toDomain() } ?: emptyList()
        }
        
        if (result is Result.Success) {
            doctorDao.insertDoctors(result.data.map { it.toEntity() })
        }
        
        return result
    }

    override suspend fun getDoctorById(id: String): Result<Doctor> {
        val cached = doctorDao.getDoctorById(id)
        if (cached != null) {
            return Result.Success(cached.toDomain())
        }
        
        return safeApiCall {
            val response = api.getDoctorById(id)
            val doctor = response.data!!.toDomain()
            doctorDao.insertDoctors(listOf(doctor.toEntity()))
            doctor
        }
    }

    override suspend fun toggleFavorite(doctorId: String): Result<Unit> {
        return safeApiCall {
            api.toggleFavorite(doctorId)
            Unit
        }
    }

    override suspend fun getFavorites(page: Int, size: Int): Result<List<Doctor>> {
        return safeApiCall {
            val response = api.getFavorites(page, size)
            response.data?.content?.map { it.toDomain() } ?: emptyList()
        }
    }
}
