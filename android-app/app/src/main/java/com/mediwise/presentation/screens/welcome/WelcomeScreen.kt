package com.mediwise.presentation.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mediwise.presentation.components.MediWiseLogo
import com.mediwise.presentation.components.MediWisePillButton
import com.mediwise.presentation.components.MediWiseSecondaryPillButton
import com.mediwise.presentation.navigation.Screen
import com.mediwise.presentation.theme.SubtitleGray
import com.mediwise.presentation.theme.SurfaceWhite

@Composable
fun WelcomeScreen(
    navController: NavController,
    onNavigateToOtp: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Center Content: Logo, Brand Name & Tagline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            MediWiseLogo(
                emblemSize = 96.dp,
                isWhiteTheme = false,
                showText = true,
                tagline = "Clinical Consultation Center"
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Real-time chat & video consultation, AI symptom analysis, and intelligent triage support at your fingertips.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = SubtitleGray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // Bottom Action Buttons: Log In & Sign Up
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MediWisePillButton(
                text = "Log In",
                onClick = { navController.navigate(Screen.Login.route) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            MediWiseSecondaryPillButton(
                text = "Sign Up",
                onClick = { navController.navigate(Screen.Signup.route) }
            )
        }
    }
}