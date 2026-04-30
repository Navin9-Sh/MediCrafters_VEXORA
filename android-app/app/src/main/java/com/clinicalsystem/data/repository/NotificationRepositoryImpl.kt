package com.clinicalsystem.data.repository

import com.clinicalsystem.core.network.safeApiCall
import com.clinicalsystem.core.result.Result
import com.clinicalsystem.data.remote.api.NotificationApi
import com.clinicalsystem.data.remote.dto.toDomain
import com.clinicalsystem.domain.model.Notification
import com.clinicalsystem.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val api: NotificationApi
) : NotificationRepository {
    
    override suspend fun getNotifications(page: Int, size: Int): Result<List<Notification>> {
        return safeApiCall {
            val response = api.getNotifications(page, size)
            response.data?.content?.map { it.toDomain() } ?: emptyList()
        }
    }

    override suspend fun markRead(id: String): Result<Unit> {
        return safeApiCall {
            api.markRead(id)
            Unit
        }
    }

    override suspend fun markAllRead(): Result<Unit> {
        return safeApiCall {
            api.markAllRead()
            Unit
        }
    }

    override suspend fun registerFcmToken(token: String): Result<Unit> {
        return safeApiCall {
            api.registerFcmToken(token)
            Unit
        }
    }
}
