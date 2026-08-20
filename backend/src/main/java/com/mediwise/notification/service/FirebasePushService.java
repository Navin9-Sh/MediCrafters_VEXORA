package com.mediwise.notification.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FirebasePushService {

    public void sendToUser(java.util.UUID userId, String title, String body) {
        // TODO: Retrieve FCM token for user and send
        log.info("Mock FCM push to user {}: {} - {}", userId, title, body);
    }

    public void sendToToken(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : Map.of())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.debug("FCM sent: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM send failed for token {}: {} ({})",
                    fcmToken, e.getMessage(), e.getMessagingErrorCode());
        }
    }

    public void sendToMultiple(List<String> tokens, String title, String body) {
        if (tokens.isEmpty()) return;
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("FCM multicast: {} success / {} failure",
                    response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast failed: {}", e.getMessage());
        }
    }
}
