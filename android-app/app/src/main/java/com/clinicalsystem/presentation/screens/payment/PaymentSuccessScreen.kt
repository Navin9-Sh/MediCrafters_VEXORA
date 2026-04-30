package com.clinicalsystem.presentation.screens.payment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.clinicalsystem.presentation.components.AppButton
import com.clinicalsystem.presentation.navigation.Screen
import com.clinicalsystem.presentation.theme.*
import kotlinx.coroutines.delay

@Composable
fun PaymentSuccessScreen(
    navController: NavController,
    paymentId: String
) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(200)
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {

            // Animated success circle
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .size(120.dp)
                    .background(AccentGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(60.dp))
            }

            Spacer(Modifier.height(32.dp))

            Text("Congratulations! 🎉", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.Center)

            Spacer(Modifier.height(12.dp))

            Text("Your appointment has been booked and payment was successful.", style = MaterialTheme.typography.bodyLarge, color = TextSecondary, textAlign = TextAlign.Center)

            Spacer(Modifier.height(8.dp))
            Text("Ref: ${paymentId.take(8).uppercase()}", style = MaterialTheme.typography.labelMedium, color = TextTertiary)

            Spacer(Modifier.height(48.dp))

            AppButton(
                text = "View Appointment",
                onClick = {
                    navController.navigate(Screen.Appointments.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { navController.navigate(Screen.Home.route) { popUpTo(0) } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Go to Home") }
        }
    }
}
