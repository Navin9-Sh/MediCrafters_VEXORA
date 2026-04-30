package com.clinicalsystem.presentation.screens.profile

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
import com.clinicalsystem.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("John Doe") }
    var phone by remember { mutableStateOf("+91 98765 43210") }
    var dob by remember { mutableStateOf("01 Jan 1997") }
    var gender by remember { mutableStateOf("Male") }
    var bloodType by remember { mutableStateOf("A+") }
    var address by remember { mutableStateOf("Mumbai, Maharashtra") }
    var emergencyContact by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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
                            isSaving = true
                            onSaveClick()
                        }
                    ) {
                        if (isSaving) {
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
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                SectionHeader("Personal Information")
                ProfileTextField("Full Name", fullName, Icons.Default.Person) { fullName = it }
                Spacer(Modifier.height(12.dp))
                ProfileTextField("Phone Number", phone, Icons.Default.Phone) { phone = it }
                Spacer(Modifier.height(12.dp))
                ProfileTextField("Date of Birth", dob, Icons.Default.CalendarMonth) { dob = it }
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
                SectionHeader("Location & Emergency")
                ProfileTextField("Address", address, Icons.Default.LocationOn) { address = it }
                Spacer(Modifier.height(12.dp))
                ProfileTextField(
                    "Emergency Contact",
                    emergencyContact,
                    Icons.Default.ContactPhone,
                    placeholder = "e.g. +91 98765 43210"
                ) { emergencyContact = it }
            }

            item { Spacer(Modifier.height(8.dp)) }
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

@OptIn(ExperimentalMaterial3Api::class)
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
                val isSelected = selected == option
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) PrimaryBlue else SurfaceWhite,
                    onClick = { onSelect(option) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        option,
                        fontSize = 12.sp,
                        color = if (isSelected) androidx.compose.ui.graphics.Color.White else TextSecondary,
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
                modifier = Modifier.fillMaxWidth().menuAnchor()
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
