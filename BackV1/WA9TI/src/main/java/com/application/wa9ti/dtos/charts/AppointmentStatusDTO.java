package com.application.wa9ti.dtos.charts;

import com.application.wa9ti.models.Appointment;

public record AppointmentStatusDTO(Appointment.Status status, Long count) {

    public static AppointmentStatusDTO fromEntity(Object[] result) {
        return new AppointmentStatusDTO(
                (Appointment.Status) result[0],  // Statut du rendez-vous
                (Long) result[1]     // Nombre de rendez-vous pour ce statut
        );
    }
}