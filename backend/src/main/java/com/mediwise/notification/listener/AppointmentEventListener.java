package com.mediwise.notification.listener;

import com.mediwise.appointment.event.AppointmentBookedEvent;
import com.mediwise.appointment.model.Appointment;
import com.mediwise.doctor.repository.DoctorRepository;
import com.mediwise.notification.model.Notification;
import com.mediwise.notification.repository.NotificationRepository;
import com.mediwise.notification.service.FirebasePushService;
import com.mediwise.profile.repository.PatientProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentEventListener {

    private final NotificationRepository notificationRepository;
    private final FirebasePushService fcmService;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorRepository doctorRepository;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Async
    @EventListener
    public void onAppointmentBooked(AppointmentBookedEvent event) {
        Appointment appt = event.getAppointment();
        if (appt == null) return;

        // Notify patient
        if (appt.getPatientId() != null) {
            patientProfileRepository.findById(appt.getPatientId()).ifPresent(patient -> {
                saveAndPush(
                        patient.getUserId(),
                        "Appointment Scheduled",
                        "Your appointment has been scheduled successfully.",
                        "APPOINTMENT_BOOKED",
                        appt.getId()
                );
            });
        }

        // Notify doctor
        if (appt.getDoctorId() != null) {
            doctorRepository.findById(appt.getDoctorId()).ifPresent(doctor -> {
                saveAndPush(
                        doctor.getUserId(),
                        "New Appointment Request",
                        "You have a new appointment request from a patient.",
                        "NEW_APPOINTMENT",
                        appt.getId()
                );
            });
        }
    }

    private void saveAndPush(UUID userId, String title, String body,
                             String type, UUID refId) {
        if (userId == null) return;
        try {
            notificationRepository.save(Notification.builder()
                    .userId(userId)
                    .title(title)
                    .body(body)
                    .type(type)
                    .refId(refId)
                    .build());

            String fcmToken = redisTemplate != null ? (String) redisTemplate.opsForValue().get("fcm_token:" + userId) : null;
            if (fcmToken != null) {
                fcmService.sendToToken(fcmToken, title, body,
                        Map.of("type", type, "refId", refId != null ? refId.toString() : ""));
            }
        } catch (Exception e) {
            log.warn("Failed to deliver notification to user {}: {}", userId, e.getMessage());
        }
    }
}
