package com.mediwise.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    var pushNotifications by remember { mutableStateOf(true) }
    var appointmentReminders by remember { mutableStateOf(true) }
    var chatNotifications by remember { mutableStateOf(true) }
    var marketingEmails by remember { mutableStateOf(false) }
    var biometricLogin by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                SettingsSection(title = "Notifications") {
                    ToggleSetting(
                        icon = Icons.Default.Notifications,
                        title = "Push Notifications",
                        subtitle = "Get alerts for appointments and updates",
                        checked = pushNotifications,
                        onCheckedChange = { pushNotifications = it }
                    )
                    HorizontalDivider(color = Divider)
                    ToggleSetting(
                        icon = Icons.Default.Alarm,
                        title = "Appointment Reminders",
                        subtitle = "1 hour before your consultation",
                        checked = appointmentReminders,
                        onCheckedChange = { appointmentReminders = it }
                    )
                    HorizontalDivider(color = Divider)
                    ToggleSetting(
                        icon = Icons.Default.Chat,
                        title = "Chat Notifications",
                        subtitle = "New messages from doctors",
                        checked = chatNotifications,
                        onCheckedChange = { chatNotifications = it }
                    )
                    HorizontalDivider(color = Divider)
                    ToggleSetting(
                        icon = Icons.Default.Email,
                        title = "Marketing Emails",
                        subtitle = "Health tips and promotions",
                        checked = marketingEmails,
                        onCheckedChange = { marketingEmails = it }
                    )
                }
            }

            item {
                SettingsSection(title = "Security") {
                    ToggleSetting(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Login",
                        subtitle = "Use fingerprint or face ID",
                        checked = biometricLogin,
                        onCheckedChange = { biometricLogin = it }
                    )
                    HorizontalDivider(color = Divider)
                    ActionSetting(
                        icon = Icons.Default.Lock,
                        title = "Change Password",
                        subtitle = "Last changed 30 days ago"
                    )
                    HorizontalDivider(color = Divider)
                    ActionSetting(
                        icon = Icons.Default.Devices,
                        title = "Active Sessions",
                        subtitle = "Manage logged-in devices"
                    )
                }
            }

            item {
                SettingsSection(title = "Appearance") {
                    ToggleSetting(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Switch to dark theme",
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                }
            }

            item {
                SettingsSection(title = "Data & Privacy") {
                    ActionSetting(
                        icon = Icons.Default.Download,
                        title = "Download My Data",
                        subtitle = "Export your health records"
                    )
                    HorizontalDivider(color = Divider)
                    ActionSetting(
                        icon = Icons.Default.DeleteForever,
                        title = "Delete Account",
                        subtitle = "Permanently remove your account",
                        isDestructive = true
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
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
private fun ToggleSetting(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(shape = RoundedCornerShape(10.dp), color = PrimaryBlueLight, modifier = Modifier.size(38.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = SurfaceWhite, checkedTrackColor = PrimaryBlue)
        )
    }
}

@Composable
private fun ActionSetting(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isDestructive) ErrorRed.copy(alpha = 0.1f) else PrimaryBlueLight,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null,
                    tint = if (isDestructive) ErrorRed else PrimaryBlue,
                    modifier = Modifier.size(20.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = if (isDestructive) ErrorRed else TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null,
            tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}

private fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(onClick = onClick))
