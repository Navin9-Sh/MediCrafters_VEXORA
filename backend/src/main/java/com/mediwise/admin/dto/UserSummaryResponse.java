package com.mediwise.admin.dto;

import com.mediwise.auth.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private UUID id;
    private String email;
    private String phone;
    private String fullName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    private User.Role role;
    private boolean active;
    private String firebaseUid;
    private Instant createdAt;
    private Instant updatedAt;
}
