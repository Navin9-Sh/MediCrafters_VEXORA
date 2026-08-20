package com.mediwise.domain.usecase.auth

import com.mediwise.core.result.Result
import com.mediwise.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(firebaseToken: String): Result<Unit> = repo.login(firebaseToken)
}
