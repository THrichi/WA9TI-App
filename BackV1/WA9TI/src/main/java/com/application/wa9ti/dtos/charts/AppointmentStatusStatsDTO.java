package com.application.wa9ti.dtos.charts;

import com.application.wa9ti.models.Appointment;

import java.time.LocalDate;

public record AppointmentStatusStatsDTO(LocalDate date, Appointment.Status status, Long count) {

    public static AppointmentStatusStatsDTO fromEntity(Object[] result) {
        return new AppointmentStatusStatsDTO(
                (LocalDate) result[0],  // Date du rendez-vous
                (Appointment.Status) result[1],     // Statut du rendez-vous
                (Long) result[2]        // Nombre de rendez-vous pour ce statut et cette date
        );
    }
}