package com.clinicalsystem.auth.service;

import com.clinicalsystem.auth.dto.AuthResponse;
import com.clinicalsystem.auth.dto.LoginRequest;
import com.clinicalsystem.auth.dto.RegisterRequest;
import com.clinicalsystem.auth.model.User;
import com.clinicalsystem.auth.repository.UserRepository;
import com.clinicalsystem.auth.security.FirebaseTokenVerifier;
import com.clinicalsystem.common.exception.BusinessException;
import com.clinicalsystem.common.exception.UnauthorizedException;
import com.clinicalsystem.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final JwtUtil jwtUtil;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final long REFRESH_EXPIRY_DAYS = 7;
    private static final long ACCESS_EXPIRY_SECONDS = 900;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String firebaseUid = firebaseTokenVerifier.verify(request.getFirebaseIdToken());

        if (userRepository.existsByFirebaseUid(firebaseUid)) {
            throw new BusinessException("ALREADY_REGISTERED", "An account already exists. Please log in.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_TAKEN", "This email is already associated with an account.");
        }

        User user = User.builder()
                .firebaseUid(firebaseUid)
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .active(true)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {} [{}]", user.getEmail(), user.getRole());
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        String firebaseUid = firebaseTokenVerifier.verify(request.getFirebaseIdToken());

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new UnauthorizedException("Account not found. Please register first."));

        if (!user.isActive()) {
            throw new UnauthorizedException("Your account has been suspended. Contact support.");
        }

        log.info("User logged in: {}", user.getEmail());
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
        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return buildAuthResponse(user);
    }

    public void logout(String accessToken) {
        if (jwtUtil.isTokenValid(accessToken)) {
            String jti = jwtUtil.extractJti(accessToken);
            long remaining = jwtUtil.extractExpiration(accessToken).getTime() - System.currentTimeMillis();
            if (remaining > 0 && redisTemplate != null) {
                redisTemplate.opsForValue().set("blacklist:" + jti, true,
                        Duration.ofMillis(remaining));
            }
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "email", user.getEmail()
        );
        String access = jwtUtil.generateAccessToken(user.getId().toString(), claims);
        String refresh = jwtUtil.generateRefreshToken(user.getId().toString());

        // Cache refresh token in Redis
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(
                    "session:" + user.getId(),
                    user.getRole().name(),
                    Duration.ofDays(REFRESH_EXPIRY_DAYS)
            );
        }

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .expiresIn(ACCESS_EXPIRY_SECONDS)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .build())
                .build();
    }
}
