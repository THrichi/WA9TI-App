package com.application.wa9ti.dtos.charts;


public record ServiceBookingDTO(String serviceName, Long count) {

    public static ServiceBookingDTO fromEntity(Object[] result) {
        return new ServiceBookingDTO(
                (String) result[0],  // Nom du service
                (Long) result[1]     // Nombre de réservations pour ce service
        );
    }
}