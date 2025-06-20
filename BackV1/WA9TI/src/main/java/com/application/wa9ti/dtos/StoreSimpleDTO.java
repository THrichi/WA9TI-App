package com.application.wa9ti.dtos;


import com.application.wa9ti.models.Store;

public record StoreSimpleDTO(
        Long id,
        String name,
        String storeUrl,
        String type,
        String image,
        String address
) {
    public static StoreSimpleDTO fromEntity(Store store) {
        return new StoreSimpleDTO(
                store.getId(),
                store.getName(),
                store.getStoreUrl(),
                store.getType(),
                store.getImage(),
                store.getAddress()
        );
    }
}