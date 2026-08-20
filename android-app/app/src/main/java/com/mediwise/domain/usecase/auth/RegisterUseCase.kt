package com.mediwise.domain.usecase.auth

import com.mediwise.core.result.Result
import com.mediwise.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(token: String, email: String, phone: String, role: String): Result<Unit> =
        repo.register(token, email, phone, role)
}
