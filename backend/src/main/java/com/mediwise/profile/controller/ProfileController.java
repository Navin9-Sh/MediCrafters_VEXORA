package com.mediwise.profile.controller;

import com.mediwise.auth.model.User;
import com.mediwise.common.response.ApiResponse;
import com.mediwise.profile.dto.UpdateProfileRequest;
import com.mediwise.profile.model.PatientProfile;
import com.mediwise.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Patient profile management and image upload")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<PatientProfile>> getProfile(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(profileService.getOrCreateProfile(user)));
    }

    @PutMapping
    @Operation(summary = "Update profile details")
    public ResponseEntity<ApiResponse<PatientProfile>> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(profileService.updateProfile(user, request)));
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile image to S3")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(profileService.uploadProfileImage(user, file)));
    }
}
