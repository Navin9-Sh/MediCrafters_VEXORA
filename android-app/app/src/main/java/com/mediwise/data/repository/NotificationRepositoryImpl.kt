package com.mediwise.data.repository

import com.mediwise.core.network.safeApiCall
import com.mediwise.core.result.Result
import com.mediwise.data.remote.api.NotificationApi
import com.mediwise.data.remote.dto.toDomain
import com.mediwise.domain.model.Notification
import com.mediwise.domain.repository.NotificationRepository
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
