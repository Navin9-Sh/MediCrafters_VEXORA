package com.mediwise.presentation.screens.appointment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediwise.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentReviewScreen(
    appointmentId: String,
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave a Review", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Doctor avatar / name
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = PrimaryBlueLight,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("SJ", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column {
                        Text("Dr. Sarah Johnson", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Cardiology • Apr 29, 2025", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("How was your experience?",
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Your honest review helps other patients",
                fontSize = 14.sp, color = TextSecondary)

            Spacer(Modifier.height(24.dp))

            // Star rating
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { star ->
                    IconButton(onClick = { rating = star }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "$star star",
                            tint = if (star <= rating) WarningAmber else TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            if (rating > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    listOf("", "Poor", "Fair", "Good", "Very Good", "Excellent")[rating],
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = WarningAmber
                )
            }

            Spacer(Modifier.height(24.dp))

            // Review text
            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                placeholder = { Text("Share details about your consultation...", color = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Divider
                ),
                maxLines = 6
            )
            Text(
                "${reviewText.length}/500",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    isSubmitting = true
                    // TODO: call ReviewUseCase(appointmentId, rating, reviewText)
                    onSubmitClick()
                },
                enabled = rating > 0 && !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Review", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
