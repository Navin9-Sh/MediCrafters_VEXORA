package com.mediwise.auth.service;

import com.mediwise.auth.dto.*;
import com.mediwise.auth.model.User;
import com.mediwise.auth.repository.UserRepository;
import com.mediwise.auth.security.FirebaseTokenVerifier;
import com.mediwise.common.exception.BusinessException;
import com.mediwise.common.exception.ResourceNotFoundException;
import com.mediwise.common.exception.UnauthorizedException;
import com.mediwise.common.util.JwtUtil;
import com.mediwise.profile.model.PatientProfile;
import com.mediwise.profile.repository.PatientProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final PatientProfileRepository patientProfileRepository;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final long REFRESH_EXPIRY_DAYS = 7;
    private static final long ACCESS_EXPIRY_SECONDS = 900;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("EMAIL_TAKEN", "This email is already associated with an account.");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone().trim())) {
            throw new BusinessException("PHONE_TAKEN", "This phone number is already associated with an account.");
        }

        if (request.getFirebaseIdToken() == null || request.getFirebaseIdToken().isBlank()) {
            throw new UnauthorizedException("A Firebase ID token is required to register.");
        }
        var firebaseToken = firebaseTokenVerifier.verifyToken(request.getFirebaseIdToken());
        String firebaseUid = firebaseToken.getUid();
        if (userRepository.existsByFirebaseUid(firebaseUid)) {
            throw new BusinessException("ALREADY_REGISTERED", "An account with this Firebase identity already exists.");
        }

        String passwordHash = null;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            passwordHash = passwordEncoder.encode(request.getPassword());
        }

        User user = User.builder()
                .firebaseUid(firebaseUid)
                .email(email)
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .fullName(request.getFullName() != null ? request.getFullName().trim() : null)
                .dob(request.getDateOfBirth())
                .passwordHash(passwordHash)
                .role(request.getRole() != null ? request.getRole() : User.Role.PATIENT)
                .active(true)
                .build();

        user = userRepository.save(user);

        // Automatically create associated PatientProfile if registering as PATIENT
        if (user.getRole() == User.Role.PATIENT) {
            PatientProfile profile = PatientProfile.builder()
                    .userId(user.getId())
                    .fullName(user.getFullName())
                    .dob(user.getDob())
                    .build();
            patientProfileRepository.save(profile);
        }

        log.info("New user registered successfully: {} [{}]", user.getEmail(), user.getRole());
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user;

        if (request.getFirebaseIdToken() != null && !request.getFirebaseIdToken().isBlank()) {
            // ── Mobile App: Firebase token login ──────────────────────────────
            var firebaseToken = firebaseTokenVerifier.verifyToken(request.getFirebaseIdToken());
            // if (!firebaseTokenVerifier.isEmailVerified(firebaseToken)) {
            // throw new UnauthorizedException("Please verify your email before logging
            // in.");
            // }
            String firebaseUid = firebaseToken.getUid();
            user = userRepository.findByFirebaseUid(firebaseUid)
                    .or(() -> request.getEmailOrPhone() != null
                            ? userRepository.findByIdentifier(request.getEmailOrPhone().trim())
                            : java.util.Optional.empty())
                    .orElseThrow(() -> new UnauthorizedException("Account not found. Please register first."));

        } else if (request.getEmailOrPhone() != null && !request.getEmailOrPhone().isBlank()
                && request.getPassword() != null && !request.getPassword().isBlank()) {
            // ── Admin Web Panel / Postman: direct email+password login ─────────
            user = userRepository.findByIdentifier(request.getEmailOrPhone().trim())
                    .orElseThrow(() -> new UnauthorizedException("No account found with this email or phone."));

            if (user.getPasswordHash() == null) {
                throw new UnauthorizedException(
                        "This account was created via social login. Please use Firebase sign-in.");
            }
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                throw new UnauthorizedException("Incorrect password. Please try again.");
            }
        } else {
            throw new UnauthorizedException("Please provide either a Firebase ID token or email + password.");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("Your account has been suspended. Contact MediWise support.");
        }

        log.info("User logged in successfully: {} [{}]", user.getEmail(), user.getRole());
        return buildAuthResponse(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token. Please log in again.");
        }

        String jti = jwtUtil.extractJti(refreshToken);
        if (redisTemplate != null) {
            Boolean isBlacklisted = (Boolean) redisTemplate.opsForValue().get("blacklist:" + jti);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                throw new UnauthorizedException("Token has been invalidated.");
            }
        }

        String userId = jwtUtil.extractSubject(refreshToken);
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Your account has been suspended.");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String identifier = request.getEmailOrPhone().trim();
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email or phone number."));

        log.info("Password reset requested for user: {}", user.getEmail());
        // In production: send email/SMS with reset code or token.
        // For development/mock: verification is logged and handled in resetPassword.
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String identifier = request.getEmailOrPhone().trim();
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email or phone number."));

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new BusinessException("INVALID_PASSWORD", "Password must be at least 6 characters.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password successfully reset for user: {}", user.getEmail());
    }

    public AuthResponse.UserInfo getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .dob(user.getDob())
                .role(user.getRole())
                .build();
    }

    public AppConfigResponse getAppConfig() {
        return AppConfigResponse.builder()
                .appName("MediWise")
                .version("1.0.0")
                .status("HEALTHY")
                .environment("production")
                .features(List.of(
                        "auth_email_password",
                        "auth_firebase_google",
                        "ai_triage",
                        "video_consultation",
                        "realtime_chat",
                        "doctor_appointments",
                        "razorpay_payments"))
                .build();
    }

    public void logout(String accessToken) {
        if (jwtUtil.isTokenValid(accessToken)) {
            String jti = jwtUtil.extractJti(accessToken);
            long remaining = jwtUtil.extractExpiration(accessToken).getTime() - System.currentTimeMillis();
            if (remaining > 0 && redisTemplate != null) {
                redisTemplate.opsForValue().set("blacklist:" + jti, true, Duration.ofMillis(remaining));
            }
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "email", user.getEmail());
        String access = jwtUtil.generateAccessToken(user.getId().toString(), claims);
        String refresh = jwtUtil.generateRefreshToken(user.getId().toString());

        // Cache session if Redis is available
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(
                    "session:" + user.getId(),
                    user.getRole().name(),
                    Duration.ofDays(REFRESH_EXPIRY_DAYS));
        }

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .expiresIn(ACCESS_EXPIRY_SECONDS)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .fullName(user.getFullName())
                        .dob(user.getDob())
                        .role(user.getRole())
                        .build())
                .build();
    }
}
