package com.clinicalsystem.domain.repository

import com.clinicalsystem.core.result.Result
import com.clinicalsystem.domain.model.Notification

interface NotificationRepository {
    suspend fun getNotifications(page: Int, size: Int): Result<List<Notification>>
    suspend fun markRead(id: String): Result<Unit>
    suspend fun markAllRead(): Result<Unit>
    suspend fun registerFcmToken(token: String): Result<Unit>
}
