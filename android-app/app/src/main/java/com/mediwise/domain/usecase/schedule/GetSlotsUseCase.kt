package com.mediwise.domain.usecase.schedule

import com.mediwise.core.result.Result
import com.mediwise.domain.model.SlotModel
import com.mediwise.domain.repository.SlotRepository
import javax.inject.Inject

class GetSlotsUseCase @Inject constructor(
    private val slotRepository: SlotRepository
) {
    suspend operator fun invoke(doctorId: String, date: String): Result<List<SlotModel>> {
        return slotRepository.getSlots(doctorId, date)
    }
}
