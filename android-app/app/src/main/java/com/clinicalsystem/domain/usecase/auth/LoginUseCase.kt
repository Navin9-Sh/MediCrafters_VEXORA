package com.clinicalsystem.domain.usecase.auth

import com.clinicalsystem.core.result.Result
import com.clinicalsystem.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(firebaseToken: String): Result<Unit> = repo.login(firebaseToken)
}
