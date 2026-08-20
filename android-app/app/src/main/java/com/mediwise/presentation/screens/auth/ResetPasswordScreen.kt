package com.mediwise.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mediwise.presentation.components.MediWiseInputField
import com.mediwise.presentation.components.MediWisePillButton
import com.mediwise.presentation.components.MediWiseTopBar
import com.mediwise.presentation.theme.*

@Composable
fun ResetPasswordScreen(
    onBackClick: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = SurfaceWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceWhite)
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Top Navigation Bar
            MediWiseTopBar(
                title = "Set Password",
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Enter your email and Firebase will send a secure password reset link.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = SubtitleGray,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            MediWiseInputField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = "Email",
                placeholder = "example@example.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Password Field
            MediWiseInputField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = "Password",
                placeholder = "••••••••••••",
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = InputPlaceholder
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Confirm Password Field
            MediWiseInputField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = "Confirm Password",
                placeholder = "••••••••••••",
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = errorMessage != null,
                errorMessage = errorMessage,
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = InputPlaceholder
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Action Button
            MediWisePillButton(
                text = "Send Reset Link",
                onClick = {
                    if (email.isBlank()) {
                        errorMessage = "Email is required"
                    } else {
                        viewModel.sendPasswordResetEmail(email) { success, error ->
                            if (success) onResetSuccess() else errorMessage = error
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
