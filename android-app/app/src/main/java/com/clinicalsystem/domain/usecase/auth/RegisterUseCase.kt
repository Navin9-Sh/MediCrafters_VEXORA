package com.clinicalsystem.domain.usecase.auth

import com.clinicalsystem.core.result.Result
import com.clinicalsystem.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(token: String, email: String, phone: String, role: String): Result<Unit> =
        repo.register(token, email, phone, role)
}
