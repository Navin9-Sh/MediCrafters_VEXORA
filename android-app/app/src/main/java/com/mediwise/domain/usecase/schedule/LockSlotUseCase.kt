package com.mediwise.domain.usecase.schedule

import com.mediwise.core.result.Result
import com.mediwise.domain.model.SlotLockResult
import com.mediwise.domain.repository.SlotRepository
import javax.inject.Inject

class LockSlotUseCase @Inject constructor(
    private val slotRepository: SlotRepository
) {
    suspend operator fun invoke(slotId: String): Result<SlotLockResult> {
        return slotRepository.lockSlot(slotId)
    }
}
