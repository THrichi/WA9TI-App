package com.application.wa9ti.dtos.charts;


import java.time.LocalDate;

public record AppointmentIncomeDTO(LocalDate date, Double totalIncome) {

    public static AppointmentIncomeDTO fromEntity(Object[] result) {
        return new AppointmentIncomeDTO(
                (LocalDate) result[0],  // Date du rendez-vous
                (Double) result[1]      // Somme des prix des rendez-vous complétés ce jour-là
        );
    }
}