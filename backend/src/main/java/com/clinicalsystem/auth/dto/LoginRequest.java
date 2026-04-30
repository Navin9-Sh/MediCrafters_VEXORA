package com.clinicalsystem.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Firebase ID token is required")
    private String firebaseIdToken;
}
