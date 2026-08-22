package com.mediwise.presentation.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mediwise.presentation.theme.*

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
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    // Photo picker launcher for bonus avatar upload
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                val mimeType = context.contentResolver.getType(it) ?: "image/jpeg"
                if (bytes != null) {
                    viewModel.uploadAvatar(bytes, mimeType)
                }
            } catch (_: Exception) {}
        }
    }

    val initials = remember(profile?.fullName) {
        profile?.fullName?.split(" ")
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { it.firstOrNull()?.toString() }
            ?.take(2)
            ?.joinToString("")
            ?.uppercase() ?: "U"
    }

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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!profile?.profileImageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = profile?.profileImageUrl,
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        initials,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = PrimaryBlue,
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .clickable { imagePickerLauncher.launch("image/*") }
                            ) {
                                if (uiState.isUploadingImage) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Upload Photo",
                                        tint = Color.White,
                                        modifier = Modifier.padding(6.dp).size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            profile?.fullName?.ifBlank { "User" } ?: "User",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        if (!profile?.email.isNullOrBlank()) {
                            Text(
                                profile?.email ?: "",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp
                            )
                        }
                        if (!profile?.phone.isNullOrBlank()) {
                            Text(
                                profile?.phone ?: "",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onEditClick,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Edit Profile", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Real Stats Cards Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileStatCard("${uiState.totalAppointments}", "Appointments", Modifier.weight(1f))
                    ProfileStatCard("${uiState.totalDoctors}", "Doctors", Modifier.weight(1f))
                    ProfileStatCard(profile?.bloodType?.ifBlank { "--" } ?: "--", "Blood Type", Modifier.weight(1f))
                    ProfileStatCard(uiState.calculatedAge, "Age", Modifier.weight(1f))
                }
            }

            // Health Section
            item {
                ProfileSection(title = "Health Information") {
                    ProfileMenuItem(
                        icon = Icons.Default.FavoriteBorder,
                        title = "Blood Group & Vitals",
                        subtitle = "Blood Type: ${profile?.bloodType?.ifBlank { "Not set" } ?: "Not set"}"
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.CalendarToday,
                        title = "Date of Birth",
                        subtitle = profile?.dateOfBirth?.ifBlank { "Not set" } ?: "Not set"
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.PersonOutline,
                        title = "Gender",
                        subtitle = profile?.gender?.ifBlank { "Not set" } ?: "Not set"
                    )
                }
            }

            // Contact & Emergency Section
            item {
                ProfileSection(title = "Contact & Emergency (Bonus)") {
                    if (!profile?.address.isNullOrBlank()) {
                        ProfileMenuItem(
                            icon = Icons.Default.LocationOn,
                            title = "Address",
                            subtitle = profile?.address ?: ""
                        )
                    }
                    ProfileMenuItem(
                        icon = Icons.Default.ContactPhone,
                        title = "Emergency Contact",
                        subtitle = if (!profile?.emergencyContact.isNullOrBlank())
                            "Tap to dial: ${profile?.emergencyContact}"
                        else
                            "Tap Edit Profile to set emergency contact",
                        onClick = {
                            profile?.emergencyContact?.takeIf { it.isNotBlank() }?.let { num ->
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
                                context.startActivity(dialIntent)
                            } ?: onEditClick()
                        }
                    )
                }
            }

            // Account & Settings Section
            item {
                ProfileSection(title = "Account & Preferences") {
                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications & Reminders",
                        subtitle = "Manage appointment & chat alerts",
                        onClick = onNotificationsClick
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        title = "App Settings",
                        subtitle = "Theme, biometrics, security",
                        onClick = onSettingsClick
                    )
                }
            }

            // Sign Out Action
            item {
                Spacer(Modifier.height(12.dp))
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
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(18.dp)
                    )
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
        Text(
            title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
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
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}