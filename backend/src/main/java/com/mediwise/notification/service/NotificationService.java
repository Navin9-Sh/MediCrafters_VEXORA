package com.mediwise.notification.service;

import com.mediwise.notification.model.Notification;
import com.mediwise.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FirebasePushService firebasePushService;

    /**
     * Persists a notification to PostgreSQL and dispatches an FCM push.
     */
    @Transactional
    public Notification send(UUID userId, String title, String body, String type, UUID refId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setType(type);
        notification.setRefId(refId);
        Notification saved = notificationRepository.save(notification);

        // Fire-and-forget FCM push (failures logged, never thrown)
        try {
            firebasePushService.sendToUser(userId, title, body);
        } catch (Exception ex) {
            log.warn("FCM push failed for user {}: {}", userId, ex.getMessage());
        }

        return saved;
    }

    /**
     * Get paginated notifications for a user, newest first.
     */
    @Transactional(readOnly = true)
    public Page<Notification> getForUser(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId, pageable);
    }

    /**
     * Mark a single notification as read.
     */
    @Transactional
    public void markRead(UUID notificationId, UUID userId) {
        notificationRepository.findByIdAndUserId(notificationId, userId)
                .ifPresent(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                });
    }

    /**
     * Mark all notifications for a user as read.
     */
    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    /**
     * Count unread notifications (used for badge counts).
     */
    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
}
