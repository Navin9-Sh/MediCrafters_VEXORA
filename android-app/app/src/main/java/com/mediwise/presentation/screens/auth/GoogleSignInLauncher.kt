package com.mediwise.presentation.screens.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.runtime.remember
import com.mediwise.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun rememberGoogleSignInLauncher(
    onToken: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val activity = LocalContext.current as? Activity
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            val token = account.idToken
            if (token.isNullOrBlank()) {
                onError("Google did not return an ID token.")
            } else {
                onToken(token)
            }
        } catch (exception: ApiException) {
            onError("Google Sign-In failed. Please try again.")
        }
    }

    return remember {
        {
            if (BuildConfig.GOOGLE_WEB_CLIENT_ID.startsWith("REPLACE_")) {
                onError("Google Sign-In is not configured for this Firebase project.")
            } else {
                val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .requestEmail()
                    .build()
                activity?.let {
                    launcher.launch(GoogleSignIn.getClient(it, options).signInIntent)
                } ?: onError("Google Sign-In is unavailable in this context.")
            }
        }
    }
}
