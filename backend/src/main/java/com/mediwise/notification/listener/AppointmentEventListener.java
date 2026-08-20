package com.mediwise.notification.listener;

import com.mediwise.appointment.event.AppointmentBookedEvent;
import com.mediwise.appointment.model.Appointment;
import com.mediwise.auth.model.User;
import com.mediwise.auth.repository.UserRepository;
import com.mediwise.notification.model.Notification;
import com.mediwise.notification.repository.NotificationRepository;
import com.mediwise.notification.service.FirebasePushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentEventListener {

    private final NotificationRepository notificationRepository;
    private final FirebasePushService fcmService;
    private final UserRepository userRepository;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Async
    @EventListener
    public void onAppointmentBooked(AppointmentBookedEvent event) {
        Appointment appt = event.getAppointment();

        // Notify patient
        saveAndPush(
                appt.getPatientId(),
                "Appointment Confirmed 🗓️",
                "Your appointment has been booked successfully.",
                "APPOINTMENT_BOOKED",
                appt.getId()
        );

        // Notify doctor
        saveAndPush(
                appt.getDoctorId(),
                "New Appointment Request 👨‍⚕️",
                "You have a new appointment request from a patient.",
                "NEW_APPOINTMENT",
                appt.getId()
        );
    }

    private void saveAndPush(java.util.UUID userId, String title, String body,
                              String type, java.util.UUID refId) {
        // Save to DB
        notificationRepository.save(Notification.builder()
                .userId(userId)
                .title(title)
                .body(body)
                .type(type)
                .refId(refId)
                .build());

        // Push via FCM
        String fcmToken = redisTemplate != null ? (String) redisTemplate.opsForValue().get("fcm_token:" + userId) : null;
        if (fcmToken != null) {
            fcmService.sendToToken(fcmToken, title, body,
                    Map.of("type", type, "refId", refId.toString()));
        }
    }
}
