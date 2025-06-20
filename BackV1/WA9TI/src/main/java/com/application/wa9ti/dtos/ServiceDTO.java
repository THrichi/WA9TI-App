package com.application.wa9ti.dtos;

import com.application.wa9ti.models.Service;

public record ServiceDTO(
        Long id,
        String name,
        int time,
        double price,
        String description,
        boolean isActif,
        String categoryName,
        Long storeId // Référence au store sans inclure l'objet complet
) {
    public static ServiceDTO fromEntity(Service service) {
        return new ServiceDTO(
                service.getId(),
                service.getName(),
                service.getTime(),
                service.getPrice(),
                service.getDescription(),
                service.isActif(),
                service.getCategoryName(),
                service.getStore() != null ? service.getStore().getId() : null
        );
    }
}