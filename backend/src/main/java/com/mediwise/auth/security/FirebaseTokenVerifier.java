package com.mediwise.auth.security;

import com.mediwise.common.exception.UnauthorizedException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FirebaseTokenVerifier {

    public FirebaseToken verifyToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new UnauthorizedException("Firebase token is missing");
        }

        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (IllegalStateException e) {
            log.error("Firebase Admin SDK is not initialized", e);
            throw new UnauthorizedException("Authentication is not configured on the server.");
        } catch (FirebaseAuthException e) {
            log.warn("Firebase token verification failed: {}", e.getAuthErrorCode());
            throw new UnauthorizedException("Invalid or expired Firebase token. Please re-authenticate.");
        } catch (Exception e) {
            log.warn("Firebase verification failed: {}", e.getMessage());
            throw new UnauthorizedException("Authentication token verification failed");
        }
    }

    public String verify(String idToken) {
        return verifyToken(idToken).getUid();
    }

    public boolean isEmailVerified(FirebaseToken token) {
        Object verified = token.getClaims().get("email_verified");
        Object phoneNumber = token.getClaims().get("phone_number");
        return Boolean.TRUE.equals(verified) || (phoneNumber != null && !phoneNumber.toString().isBlank());
    }
}
