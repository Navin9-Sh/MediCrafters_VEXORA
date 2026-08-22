package com.mediwise.appointment.service;

import com.mediwise.appointment.dto.BookAppointmentRequest;
import com.mediwise.appointment.dto.AppointmentResponse;
import com.mediwise.appointment.dto.CancelRequest;
import com.mediwise.appointment.event.AppointmentBookedEvent;
import com.mediwise.appointment.model.Appointment;
import com.mediwise.appointment.repository.AppointmentRepository;
import com.mediwise.auth.model.User;
import com.mediwise.common.exception.BusinessException;
import com.mediwise.common.exception.ResourceNotFoundException;
import com.mediwise.common.exception.SlotConflictException;
import com.mediwise.doctor.model.Doctor;
import com.mediwise.doctor.repository.DoctorRepository;
import com.mediwise.profile.model.PatientProfile;
import com.mediwise.profile.repository.PatientProfileRepository;
import com.mediwise.schedule.model.TimeSlot;
import com.mediwise.schedule.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository slotRepository;
    private final DoctorRepository doctorRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @CacheEvict(value = "slots", allEntries = true)
    public AppointmentResponse bookAppointment(User user, BookAppointmentRequest request) {
        PatientProfile patient = patientProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    // Auto-initialize profile if registering directly
                    PatientProfile newProfile = PatientProfile.builder()
                            .userId(user.getId())
                            .fullName(user.getFullName() != null ? user.getFullName() : "Patient")
                            .dob(user.getDob())
                            .build();
                    return patientProfileRepository.save(newProfile);
                });

        TimeSlot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot", request.getSlotId().toString()));

        // Verify the lock belongs to this user
        if (slot.getStatus() != TimeSlot.SlotStatus.LOCKED ||
                !user.getId().equals(slot.getLockedBy())) {
            throw new SlotConflictException("Slot is not reserved for you. Please re-select.");
        }

        // Mark slot as BOOKED
        slot.setStatus(TimeSlot.SlotStatus.BOOKED);
        slot.setLockedBy(null);
        slot.setLockedUntil(null);
        slotRepository.save(slot);

        Appointment appointment = Appointment.builder()
                .patientId(patient.getId())
                .doctorId(request.getDoctorId())
                .slotId(slot.getId())
                .type(request.getType())
                .chiefComplaint(request.getChiefComplaint())
                .status(Appointment.AppointmentStatus.PENDING)
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Appointment {} booked by patient {}", appointment.getId(), patient.getId());

        // Publish domain event → triggers push notifications asynchronously
        eventPublisher.publishEvent(new AppointmentBookedEvent(this, appointment));

        return buildAppointmentResponse(appointment, slot);
    }

    public Page<AppointmentResponse> getMyAppointments(User user, String status, int page, int size) {
        PatientProfile patient = patientProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    PatientProfile newProfile = PatientProfile.builder()
                            .userId(user.getId())
                            .fullName(user.getFullName() != null ? user.getFullName() : "Patient")
                            .dob(user.getDob())
                            .build();
                    return patientProfileRepository.save(newProfile);
                });

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (status != null && !status.isBlank()) {
            List<Appointment.AppointmentStatus> statuses = Arrays.stream(status.split(","))
                    .map(String::trim)
                    .map(s -> {
                        try {
                            return Appointment.AppointmentStatus.valueOf(s.toUpperCase());
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            if (!statuses.isEmpty()) {
                return appointmentRepository
                        .findByPatientIdAndStatusInOrderByCreatedAtDesc(patient.getId(), statuses, pageable)
                        .map(this::buildAppointmentResponse);
            }
        }

        return appointmentRepository
                .findByPatientIdOrderByCreatedAtDesc(patient.getId(), pageable)
                .map(this::buildAppointmentResponse);
    }

    public AppointmentResponse getAppointmentById(UUID id, User user) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id.toString()));
        return buildAppointmentResponse(appointment);
    }

    @Transactional
    @CacheEvict(value = "slots", allEntries = true)
    public AppointmentResponse cancelAppointment(UUID id, User user, CancelRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id.toString()));

        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED ||
                appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            throw new BusinessException("CANNOT_CANCEL",
                    "This appointment cannot be cancelled in its current status.");
        }

        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointment.setCancelledBy(user.getId());
        appointment.setCancelReason(request != null ? request.getReason() : "Cancelled by user");
        appointmentRepository.save(appointment);

        // Release the slot
        slotRepository.findById(appointment.getSlotId()).ifPresent(slot -> {
            slot.setStatus(TimeSlot.SlotStatus.AVAILABLE);
            slotRepository.save(slot);
        });

        log.info("Appointment {} cancelled by user {}", id, user.getId());
        return buildAppointmentResponse(appointment);
    }

    private AppointmentResponse buildAppointmentResponse(Appointment appointment) {
        TimeSlot slot = slotRepository.findById(appointment.getSlotId()).orElse(null);
        return buildAppointmentResponse(appointment, slot);
    }

    private AppointmentResponse buildAppointmentResponse(Appointment appointment, TimeSlot slot) {
        Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);
        String docName = doctor != null ? doctor.getFullName() : null;
        String docSpecialty = doctor != null ? doctor.getSpecialty() : null;
        String docImage = doctor != null ? doctor.getProfileImage() : null;
        return AppointmentResponse.from(appointment, slot, docName, docSpecialty, docImage);
    }
}
