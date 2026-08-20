package com.mediwise.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Optional Firebase ID token for Google / Firebase authentication
     */
    private String firebaseIdToken;

    /**
     * Email or mobile number for direct authentication
     */
    private String emailOrPhone;

    /**
     * Plaintext password for direct authentication
     */
    private String password;
}
