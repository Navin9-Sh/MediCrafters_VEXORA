package com.mediwise.domain.repository

import com.mediwise.core.result.Result
import com.mediwise.domain.model.SlotLockResult
import com.mediwise.domain.model.SlotModel

interface SlotRepository {
    suspend fun getSlots(doctorId: String, date: String): Result<List<SlotModel>>
    suspend fun lockSlot(slotId: String): Result<SlotLockResult>
    suspend fun releaseSlot(slotId: String): Result<Unit>
}
