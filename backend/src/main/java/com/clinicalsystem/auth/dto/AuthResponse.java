package com.clinicalsystem.auth.dto;

import com.clinicalsystem.auth.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;  // seconds
    private UserInfo user;

    @Data @Builder
    public static class UserInfo {
        private UUID id;
        private String email;
        private String phone;
        private User.Role role;
    }
}
