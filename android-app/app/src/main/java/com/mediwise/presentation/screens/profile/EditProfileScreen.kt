package com.mediwise.presentation.screens.profile

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mediwise.presentation.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var fullName by remember(profile?.fullName) { mutableStateOf(profile?.fullName ?: "") }
    var phone by remember(profile?.phone) { mutableStateOf(profile?.phone ?: "") }
    var dob by remember(profile?.dateOfBirth) { mutableStateOf(profile?.dateOfBirth ?: "") }
    var gender by remember(profile?.gender) { mutableStateOf(profile?.gender?.ifBlank { "Male" } ?: "Male") }
    var bloodType by remember(profile?.bloodType) { mutableStateOf(profile?.bloodType?.ifBlank { "O+" } ?: "O+") }
    var address by remember(profile?.address) { mutableStateOf(profile?.address ?: "") }
    var emergencyContact by remember(profile?.emergencyContact) { mutableStateOf(profile?.emergencyContact ?: "") }

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

    val initials = remember(fullName) {
        fullName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifBlank { "U" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (fullName.isBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please enter your full name")
                                }
                                return@TextButton
                            }
                            viewModel.updateProfile(
                                fullName = fullName,
                                dob = dob,
                                bloodType = bloodType,
                                gender = gender,
                                address = address,
                                emergencyContact = emergencyContact
                            ) { success ->
                                if (success) {
                                    onSaveClick()
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(uiState.error ?: "Failed to save profile")
                                    }
                                }
                            }
                        }
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryBlue
                            )
                        } else {
                            Text("Save", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundWhite
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Avatar change section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlueLight),
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
                                    color = PrimaryBlue,
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
                }
            }

            item {
                SectionHeader("Personal Information")
                ProfileTextField("Full Name", fullName, Icons.Default.Person) { fullName = it }
                Spacer(Modifier.height(12.dp))
                ProfileTextField("Phone Number", phone, Icons.Default.Phone) { phone = it }
                Spacer(Modifier.height(12.dp))
                ProfileTextField(
                    "Date of Birth (YYYY-MM-DD)",
                    dob,
                    Icons.Default.CalendarMonth,
                    placeholder = "YYYY-MM-DD"
                ) { dob = it }
            }

            item {
                SectionHeader("Health Details")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GenderSelector(
                        selected = gender,
                        onSelect = { gender = it },
                        modifier = Modifier.weight(1f)
                    )
                    BloodTypeSelector(
                        selected = bloodType,
                        onSelect = { bloodType = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SectionHeader("Location & Emergency (Bonus)")
                ProfileTextField("Address", address, Icons.Default.LocationOn, placeholder = "e.g. 123 Healthcare Ave, Mumbai") { address = it }
                Spacer(Modifier.height(12.dp))
                ProfileTextField(
                    "Emergency Contact",
                    emergencyContact,
                    Icons.Default.ContactPhone,
                    placeholder = "e.g. +91 98765 43210"
                ) { emergencyContact = it }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
    )
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String = label,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = TextSecondary) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = PrimaryBlue) },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = Divider,
            focusedLabelColor = PrimaryBlue
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun GenderSelector(selected: String, onSelect: (String) -> Unit, modifier: Modifier) {
    Column(modifier = modifier) {
        Text("Gender", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Male", "Female", "Other").forEach { option ->
                val isSelected = selected.equals(option, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) PrimaryBlue else SurfaceWhite,
                    onClick = { onSelect(option) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        option,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BloodTypeSelector(selected: String, onSelect: (String) -> Unit, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val types = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    Column(modifier = modifier) {
        Text("Blood Type", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Divider
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                types.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = { onSelect(type); expanded = false }
                    )
                }
            }
        }
    }
}

