package com.application.wa9ti.dtos;


import com.application.wa9ti.models.OpeningHours;
import com.application.wa9ti.models.Store;
import com.application.wa9ti.models.Subscription;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StoreDTO(
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
        List<String> seoKeywords,
        List<SocialNetworkDTO> socialNetworks,
        List<String> gallery,
        List<ServiceDTO> services,
        List<OpeningHoursDTO> openingHours,
        List<ReviewDTO> reviews,
        List<StoreClosureDTO> closures,
        Subscription.SubscriptionType subscription,
        Subscription.SubscriptionStatus subscriptionStatus,
        LocalDate subscriptionStartDate,
        LocalDate subscriptionEndDate
        ) {
    public static StoreDTO fromEntity(Store store) {
        return new StoreDTO(
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
                store.getSeoKeywords(),
                store.getSocialNetworks().stream().map(SocialNetworkDTO::fromEntity).toList(),
                store.getGallery(),
                store.getServices().stream().map(ServiceDTO::fromEntity).toList(),
                store.getOpeningHours().stream().map(OpeningHoursDTO::fromEntity).toList(),
                store.getReviews().stream().map(ReviewDTO::fromEntity).toList(),
                store.getClosures().stream().map(StoreClosureDTO::fromEntity).toList(),
                store.getOwner().getSubscription().getType(),
                store.getOwner().getSubscription().getStatus(),
                store.getOwner().getSubscription().getStartDate(),
                store.getOwner().getSubscription().getEndDate()
        );
    }
}

