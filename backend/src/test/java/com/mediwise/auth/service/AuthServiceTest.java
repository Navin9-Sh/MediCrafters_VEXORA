package com.mediwise.auth.service;

import com.mediwise.auth.dto.*;
import com.mediwise.auth.model.User;
import com.mediwise.auth.repository.UserRepository;
import com.mediwise.auth.security.FirebaseTokenVerifier;
import com.google.firebase.auth.FirebaseToken;
import com.mediwise.common.exception.BusinessException;
import com.mediwise.common.exception.UnauthorizedException;
import com.mediwise.common.util.JwtUtil;
import com.mediwise.profile.model.PatientProfile;
import com.mediwise.profile.repository.PatientProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FirebaseTokenVerifier firebaseTokenVerifier;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private UUID sampleUserId;

    @BeforeEach
    void setUp() {
        sampleUserId = UUID.randomUUID();
        sampleUser = User.builder()
                .id(sampleUserId)
                .email("test@mediwise.com")
                .fullName("John Doe")
                .phone("+1234567890")
                .dob(LocalDate.of(1995, 5, 20))
                .passwordHash("hashedPassword123")
                .role(User.Role.PATIENT)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should successfully register new user and create PatientProfile")
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("test@mediwise.com")
                .password("secret123")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .firebaseIdToken("verified-firebase-token")
                .role(User.Role.PATIENT)
                .build();

        when(userRepository.existsByEmail("test@mediwise.com")).thenReturn(false);
        when(userRepository.existsByPhone("+1234567890")).thenReturn(false);
        FirebaseToken registrationToken = mock(FirebaseToken.class);
        when(registrationToken.getUid()).thenReturn("firebase_uid_123");
        when(firebaseTokenVerifier.verifyToken("verified-firebase-token")).thenReturn(registrationToken);
        when(userRepository.existsByFirebaseUid("firebase_uid_123")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtUtil.generateAccessToken(anyString(), anyMap())).thenReturn("mock.access.token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("mock.refresh.token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock.access.token", response.getAccessToken());
        assertEquals("mock.refresh.token", response.getRefreshToken());
        assertEquals("test@mediwise.com", response.getUser().getEmail());
        assertEquals("John Doe", response.getUser().getFullName());
        assertEquals(User.Role.PATIENT, response.getUser().getRole());

        verify(userRepository).save(any(User.class));
        verify(patientProfileRepository).save(any(PatientProfile.class));
    }

    @Test
    @DisplayName("Should fail registration if email is already taken")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@mediwise.com")
                .password("secret123")
                .build();

        when(userRepository.existsByEmail("test@mediwise.com")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(request));
        assertEquals("EMAIL_TAKEN", exception.getCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject direct email and password login")
    void testLoginWithEmailAndPasswordSuccess() {
        LoginRequest request = LoginRequest.builder()
                .emailOrPhone("test@mediwise.com")
                .password("secret123")
                .build();

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertEquals("A Firebase ID token is required to log in.", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject incorrect direct credentials")
    void testLoginWithWrongPassword() {
        LoginRequest request = LoginRequest.builder()
                .emailOrPhone("test@mediwise.com")
                .password("wrongpassword")
                .build();

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertEquals("A Firebase ID token is required to log in.", exception.getMessage());
    }

    @Test
    @DisplayName("Should successfully login with Firebase ID token")
    void testLoginWithFirebaseToken() {
        LoginRequest request = LoginRequest.builder()
                .firebaseIdToken("mock_google_token_123")
                .build();

        FirebaseToken firebaseToken = mock(FirebaseToken.class);
        when(firebaseToken.getUid()).thenReturn("firebase_uid_123");
        doReturn(true).when(firebaseTokenVerifier).isEmailVerified(firebaseToken);
        when(firebaseTokenVerifier.verifyToken("mock_google_token_123")).thenReturn(firebaseToken);
        when(userRepository.findByFirebaseUid("firebase_uid_123")).thenReturn(Optional.of(sampleUser));
        when(jwtUtil.generateAccessToken(anyString(), anyMap())).thenReturn("mock.access.token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("mock.refresh.token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.access.token", response.getAccessToken());
        assertEquals(sampleUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    @DisplayName("Should successfully reset password")
    void testResetPasswordSuccess() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .emailOrPhone("test@mediwise.com")
                .newPassword("newPassword456")
                .build();

        when(userRepository.findByIdentifier("test@mediwise.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode("newPassword456")).thenReturn("newHashedPassword");

        authService.resetPassword(request);

        verify(passwordEncoder).encode("newPassword456");
        verify(userRepository).save(sampleUser);
        assertEquals("newHashedPassword", sampleUser.getPasswordHash());
    }

    @Test
    @DisplayName("Should return MediWise app config for splash screen")
    void testGetAppConfig() {
        AppConfigResponse config = authService.getAppConfig();

        assertNotNull(config);
        assertEquals("MediWise", config.getAppName());
        assertEquals("1.0.0", config.getVersion());
        assertEquals("HEALTHY", config.getStatus());
        assertTrue(config.getFeatures().contains("auth_email_password"));
    }
}
