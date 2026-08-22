package com.mediwise.data.repository

import com.mediwise.core.network.safeApiCall
import com.mediwise.core.result.Result
import com.mediwise.data.remote.api.SlotApi
import com.mediwise.data.remote.dto.toDomain
import com.mediwise.domain.model.SlotLockResult
import com.mediwise.domain.model.SlotModel
import com.mediwise.domain.repository.SlotRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SlotRepositoryImpl @Inject constructor(
    private val api: SlotApi
) : SlotRepository {

    override suspend fun getSlots(doctorId: String, date: String): Result<List<SlotModel>> {
        return safeApiCall {
            val response = api.getSlots(doctorId, date)
            response.data?.map { it.toDomain() } ?: emptyList()
        }
    }

    override suspend fun lockSlot(slotId: String): Result<SlotLockResult> {
        return safeApiCall {
            val response = api.lockSlot(slotId)
            val d = response.data!!
            SlotLockResult(
                slotId = d.slotId,
                locked = d.locked,
                expiresAt = d.expiresAt,
                ttlMinutes = d.ttlMinutes
            )
        }
    }

    override suspend fun releaseSlot(slotId: String): Result<Unit> {
        return safeApiCall {
            api.releaseSlot(slotId)
            Unit
        }
    }
}
