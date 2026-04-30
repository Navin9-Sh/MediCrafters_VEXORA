package com.clinicalsystem.presentation.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.clinicalsystem.presentation.components.AppButton
import com.clinicalsystem.presentation.components.AppTextField
import com.clinicalsystem.presentation.navigation.Screen
import com.clinicalsystem.presentation.theme.*

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundPrimary)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(PrimaryBlueLight, MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) { Text("+", style = MaterialTheme.typography.headlineLarge, color = PrimaryBlue) }

            Spacer(Modifier.height(24.dp))
            Text("Welcome Back", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Log in to your account", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

            Spacer(Modifier.height(40.dp))

            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email address",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = PrimaryBlue) }
            )

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryBlue) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate(Screen.ResetPassword.route) }, modifier = Modifier.align(Alignment.End)) {
                Text("Forgot Password?", color = PrimaryBlue)
            }

            Spacer(Modifier.height(24.dp))

            AppButton(
                text = "Log In",
                onClick = { viewModel.loginWithEmailPassword(email, password) },
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Divider
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                Text("  or  ", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
            }

            Spacer(Modifier.height(24.dp))

            // Google Sign In
            OutlinedButton(
                onClick = { viewModel.loginWithGoogle() },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Continue with Google", color = TextPrimary, modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text("Don't have an account? ", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { navController.navigate(Screen.Signup.route) }, contentPadding = PaddingValues(0.dp)) {
                    Text("Sign Up", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
