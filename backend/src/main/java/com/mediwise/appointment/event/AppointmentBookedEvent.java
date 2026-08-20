package com.mediwise.appointment.event;

import com.mediwise.appointment.model.Appointment;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppointmentBookedEvent extends ApplicationEvent {
    private final Appointment appointment;

    public AppointmentBookedEvent(Object source, Appointment appointment) {
        super(source);
        this.appointment = appointment;
    }
}
