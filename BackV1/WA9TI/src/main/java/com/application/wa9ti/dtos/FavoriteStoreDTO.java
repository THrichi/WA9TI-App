package com.application.wa9ti.dtos;

import com.application.wa9ti.models.Store;

public record FavoriteStoreDTO(
        Long id,
        String name,
        String storeUrl,
        String type,
        String image,
        Long appointmentCount) {

    public static FavoriteStoreDTO fromEntity(Store store, Long appointmentCount) {
        return new FavoriteStoreDTO(store.getId(), store.getName(), store.getStoreUrl(), store.getType(), store.getImage(), appointmentCount);
    }

}

