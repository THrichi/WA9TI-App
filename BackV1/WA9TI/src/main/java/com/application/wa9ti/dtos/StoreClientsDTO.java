package com.application.wa9ti.dtos;

import com.application.wa9ti.models.Store;

import java.util.List;

public record StoreClientsDTO(
        Long id,
        String name,
        String storeUrl,
        String type,
        String email,
        String phone,
        String address,
        Double latitude,
        Double longitude,
        String image,
        String description,
        List<SocialNetworkDTO> socialNetworks,
        List<String> gallery,
        List<ServiceDTO> services,
        List<OpeningHoursDTO> openingHours,
        List<ReviewDTO> reviews,
        List<StoreClosureDTO> closures,
        StoreAppointmentSettingsDTO settings
        ) {
    public static StoreClientsDTO fromEntity(Store store, StoreAppointmentSettingsDTO settings) {
        return new StoreClientsDTO(
                store.getId(),
                store.getName(),
                store.getStoreUrl(),
                store.getType(),
                store.getEmail(),
                store.getPhone(),
                store.getAddress(),
                store.getLatitude(),
                store.getLongitude(),
                store.getImage(),
                store.getDescription(),
                store.getSocialNetworks().stream().map(SocialNetworkDTO::fromEntity).toList(),
                store.getGallery(),
                store.getServices().stream().map(ServiceDTO::fromEntity).toList(),
                store.getOpeningHours().stream().map(OpeningHoursDTO::fromEntity).toList(),
                store.getReviews().stream().map(ReviewDTO::fromEntity).toList(),
                store.getClosures().stream().map(StoreClosureDTO::fromEntity).toList(),
                settings
        );
    }
}

