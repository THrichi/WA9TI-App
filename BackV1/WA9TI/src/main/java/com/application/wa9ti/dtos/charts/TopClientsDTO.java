package com.application.wa9ti.dtos.charts;

public record TopClientsDTO(String clientName, String email, String phone, String photo, Long appointmentCount) {

    public static TopClientsDTO fromEntity(Object[] result) {
        return new TopClientsDTO(
                (String) result[0],  // Nom du client
                (String) result[1],  // Email du client
                (String) result[2],  // Téléphone du client
                (String) result[3],  // Photo du client
                (Long) result[4]     // Nombre total de rendez-vous
        );
    }
}
