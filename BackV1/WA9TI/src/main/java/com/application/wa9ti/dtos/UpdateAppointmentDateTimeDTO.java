package com.application.wa9ti.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateAppointmentDateTimeDTO(
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {
    public UpdateAppointmentDateTimeDTO {
        if (date == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("La date et les horaires ne peuvent pas être null.");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("L'heure de fin ne peut pas être avant l'heure de début.");
        }
    }
}

