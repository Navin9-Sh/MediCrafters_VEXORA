package com.clinicalsystem.auth.dto;

import com.clinicalsystem.auth.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Firebase ID token is required")
    private String firebaseIdToken;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 10, max = 15, message = "Phone must be between 10-15 digits")
    private String phone;

    private User.Role role = User.Role.PATIENT;
}
