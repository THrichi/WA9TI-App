package com.application.wa9ti.dtos;

import com.application.wa9ti.models.Service;

public record StoreServiceDto(
        String name,
        int time,
        double price,
        String description,
        boolean isActif,
        String categoryName
) {
    public static StoreServiceDto fromEntity(Service service) {
        return new StoreServiceDto(
                service.getName(),
                service.getTime(),
                service.getPrice(),
                service.getDescription(),
                service.isActif(),
                service.getCategoryName()
        );
    }
}

