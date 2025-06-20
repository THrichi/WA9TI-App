package com.application.wa9ti.dtos.charts;


import java.time.LocalDate;

public record NewClientsDTO(LocalDate date, Long newClientsCount) {

    public static NewClientsDTO fromEntity(Object[] result) {
        return new NewClientsDTO(
                (LocalDate) result[0],  // Date du premier rendez-vous du client
                (Long) result[1]        // Nombre de nouveaux clients ce jour-là
        );
    }
}