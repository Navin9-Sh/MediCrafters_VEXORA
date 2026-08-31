package com.mediwise.auth.dto;

import com.mediwise.auth.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String email;
        private String phone;
        private String fullName;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dob;

        private User.Role role;

        private UUID profileId;
        private Boolean verified;
        private String specialty;
        private String profileImage;
    }
}
