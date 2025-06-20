package com.application.wa9ti.dtos.charts;


import java.time.LocalDate;
import java.time.LocalTime;

public record PopularTimesDTO(LocalDate date, LocalTime startTime, Long appointmentCount) {

    public static PopularTimesDTO fromEntity(Object[] result) {
        return new PopularTimesDTO(
                (LocalDate) result[0],  // Date du rendez-vous
                (LocalTime) result[1],  // Heure de début
                (Long) result[2]        // Nombre total de rendez-vous
        );
    }
}