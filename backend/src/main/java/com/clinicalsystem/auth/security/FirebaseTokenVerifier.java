package com.clinicalsystem.auth.security;

import com.clinicalsystem.common.exception.UnauthorizedException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FirebaseTokenVerifier {

    public String verify(String idToken) {
        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return token.getUid();
        } catch (FirebaseAuthException e) {
            log.warn("Firebase token verification failed: {}", e.getAuthErrorCode());
            throw new UnauthorizedException("Invalid or expired Firebase token. Please re-authenticate.");
        }
    }
}
