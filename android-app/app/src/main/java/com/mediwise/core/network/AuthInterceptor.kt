package com.mediwise.core.network

import com.mediwise.core.datastore.SessionDataStore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val firebaseToken = runBlocking {
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
        }
        val token = firebaseToken ?: runBlocking { sessionDataStore.accessToken.first() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
