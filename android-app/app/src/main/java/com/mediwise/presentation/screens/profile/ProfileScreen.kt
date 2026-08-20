package com.mediwise.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediwise.presentation.theme.*

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onEditClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = TextPrimary) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        containerColor = BackgroundWhite
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(PrimaryBlue, PrimaryBlueDark)))
                        .padding(top = 24.dp, bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                            val initials = profile?.fullName?.split(" ")?.let {
                                if (it.size > 1) "${it[0].first()}${it[1].first()}" else it[0].first().toString()
                            } ?: "U"
                                
                            Text(initials, color = Color.White,
                                fontWeight = FontWeight.Bold, fontSize = 28.sp)
                            }
                            Surface(
                                shape = CircleShape,
                                color = PrimaryBlue,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(26.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp).size(16.dp))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(profile?.fullName ?: "User", color = Color.White,
                            fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(profile?.email ?: "", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onEditClick,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null,
                                tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit Profile", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileStatCard("12", "Appointments", Modifier.weight(1f))
                    ProfileStatCard("5", "Doctors", Modifier.weight(1f))
                    ProfileStatCard("A+", "Blood Type", Modifier.weight(1f))
                    ProfileStatCard("28", "Age", Modifier.weight(1f))
                }
            }

            item {
                ProfileSection(title = "Health") {
                    ProfileMenuItem(Icons.Default.FavoriteBorder, "Medical History", "View records")
                    ProfileMenuItem(Icons.Default.MonitorHeart, "Vital Records", "Heart rate, BP, SpO2")
                    ProfileMenuItem(Icons.Default.Psychology, "AI Health Report", "Last checked 2 days ago")
                }
            }

            item {
                ProfileSection(title = "Account") {
                    ProfileMenuItem(Icons.Default.Notifications, "Notifications",
                        "Manage alerts", onClick = onNotificationsClick)
                    ProfileMenuItem(Icons.Default.Lock, "Privacy & Security", "Password, 2FA")
                    ProfileMenuItem(Icons.Default.Payment, "Payment Methods", "Cards, UPI, Wallets")
                    ProfileMenuItem(Icons.Default.Receipt, "Transaction History", "View all payments")
                }
            }

            item {
                ProfileSection(title = "Support") {
                    ProfileMenuItem(Icons.Default.Help, "Help Center", "FAQs and support")
                    ProfileMenuItem(Icons.Default.Info, "About", "Version 1.0.0")
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.logout()
                        onLogoutClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null,
                        tint = ErrorRed, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ProfileStatCard(value: String, label: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryBlue)
            Text(label, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = TextSecondary, modifier = Modifier.padding(vertical = 8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = PrimaryBlueLight,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null,
            tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}