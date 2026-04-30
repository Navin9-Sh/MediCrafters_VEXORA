package com.clinicalsystem.presentation.screens.doctors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clinicalsystem.presentation.theme.*

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDetailScreen(
    doctorId: String,
    onBackClick: () -> Unit,
    onBookClick: (String) -> Unit,
    viewModel: DoctorDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val doctor = uiState.doctor

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = Color.White)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = SurfaceWhite
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Chat")
                    }
                    Button(
                        onClick = { onBookClick(doctorId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Book Slot")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Hero section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Brush.verticalGradient(listOf(PrimaryBlue, PrimaryBlueDark)))
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "SJ", color = Color.White,
                                fontWeight = FontWeight.Bold, fontSize = 24.sp
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(doctor?.fullName ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(doctor?.specialty ?: "", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
            } else if (doctor != null) {

            // Stats row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(String.format("%.1f", doctor?.avgRating ?: 0.0), "Rating", Icons.Default.Star, WarningAmber, Modifier.weight(1f))
                    StatCard("${doctor?.totalReviews ?: 0}", "Reviews", Icons.Default.People, PrimaryBlue, Modifier.weight(1f))
                    StatCard("${doctor?.experienceYears ?: 0}y", "Experience", Icons.Default.WorkHistory, AccentGreen, Modifier.weight(1f))
                    StatCard("₹${doctor?.consultationFee ?: 0.0}", "Fee", Icons.Default.Payments, AIPurple, Modifier.weight(1f))
                }
            }

            // About
            item {
                SectionCard(title = "About") {
                    Text(
                        doctor?.bio ?: "",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                }
            }

            // Available slots preview
            item {
                SectionCard(title = "Available Today") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("09:00 AM", "10:30 AM", "02:00 PM", "03:30 PM", "05:00 PM")) { slot ->
                            SlotChip(time = slot, onClick = { onBookClick(doctorId) })
                        }
                    }
                }
            }

            // Reviews
            item {
                SectionCard(title = "Patient Reviews") {
                    repeat(3) { i ->
                        ReviewItem(
                            name = listOf("Priya S.", "Rahul M.", "Anjali K.")[i],
                            rating = listOf(5, 4, 5)[i],
                            comment = listOf(
                                "Excellent consultation! Very thorough and patient.",
                                "Very knowledgeable doctor. Explained everything clearly.",
                                "Highly recommended. Quick diagnosis and effective treatment."
                            )[i]
                        )
                        if (i < 2) HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }

            } // End of doctor data block
            
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
                     color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Text(label, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SlotChip(time: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = PrimaryBlueLight,
        onClick = onClick
    ) {
        Text(
            time, fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = PrimaryBlue,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ReviewItem(name: String, rating: Int, comment: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PrimaryBlueLight),
            contentAlignment = Alignment.Center
        ) {
            Text(name.first().toString(), color = PrimaryBlue, fontWeight = FontWeight.Bold)
        }
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Row {
                    repeat(rating) {
                        Icon(Icons.Default.Star, contentDescription = null,
                            tint = WarningAmber, modifier = Modifier.size(12.dp))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(comment, fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp)
        }
    }
}
