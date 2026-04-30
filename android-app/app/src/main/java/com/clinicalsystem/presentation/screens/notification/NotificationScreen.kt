package com.clinicalsystem.presentation.screens.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clinicalsystem.presentation.theme.*

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val timeAgo: String,
    val isRead: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBackClick: () -> Unit) {
    val notifications = remember {
        mutableStateListOf(
            NotificationItem("1", "Appointment Confirmed", "Your appointment with Dr. Sarah Johnson on Apr 29 at 10:00 AM is confirmed.", "APPOINTMENT_CONFIRMED", "2 min ago", false),
            NotificationItem("2", "Payment Successful", "Payment of ₹800 for your consultation has been received.", "PAYMENT_SUCCESS", "1 hr ago", false),
            NotificationItem("3", "New Message", "Dr. Raj Patel sent you a message.", "CHAT_MESSAGE", "3 hr ago", true),
            NotificationItem("4", "Appointment Reminder", "You have a consultation with Dr. Sarah Johnson tomorrow at 10:00 AM.", "APPOINTMENT_REMINDER", "1 day ago", true),
            NotificationItem("5", "Health Tip", "Stay hydrated! Aim for 8 glasses of water daily for optimal health.", "HEALTH_TIP", "2 days ago", true),
        )
    }

    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications", fontWeight = FontWeight.Bold, color = TextPrimary)
                        if (unreadCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "$unreadCount",
                                        fontSize = 11.sp,
                                        color = androidx.compose.ui.graphics.Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = {
                            notifications.replaceAll { it.copy(isRead = true) }
                        }) {
                            Text("Mark all read", color = PrimaryBlue, fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        containerColor = BackgroundWhite
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsOff, contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No notifications yet", fontSize = 16.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    NotificationRow(
                        notification = notif,
                        onClick = {
                            val idx = notifications.indexOf(notif)
                            if (idx >= 0) notifications[idx] = notif.copy(isRead = true)
                        }
                    )
                    HorizontalDivider(color = Divider)
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: NotificationItem, onClick: () -> Unit) {
    val (icon, iconColor) = notificationIcon(notification.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (!notification.isRead) PrimaryBlueLight.copy(alpha = 0.3f)
                else SurfaceWhite
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    notification.title,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(notification.body, fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp)
            Spacer(Modifier.height(6.dp))
            Text(notification.timeAgo, fontSize = 11.sp, color = TextSecondary.copy(alpha = 0.7f))
        }
    }
}

private fun notificationIcon(type: String): Pair<ImageVector, androidx.compose.ui.graphics.Color> =
    when (type) {
        "APPOINTMENT_CONFIRMED" -> Icons.Default.CheckCircle to AccentGreen
        "APPOINTMENT_REMINDER"  -> Icons.Default.Alarm to WarningAmber
        "APPOINTMENT_CANCELLED" -> Icons.Default.Cancel to ErrorRed
        "PAYMENT_SUCCESS"       -> Icons.Default.Payments to AccentGreen
        "PAYMENT_FAILED"        -> Icons.Default.ErrorOutline to ErrorRed
        "CHAT_MESSAGE"          -> Icons.Default.Chat to PrimaryBlue
        "HEALTH_TIP"            -> Icons.Default.FavoriteBorder to AIPurple
        else                    -> Icons.Default.Notifications to PrimaryBlue
    }
