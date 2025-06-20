package com.application.wa9ti.dtos;


import java.util.List;

public record OwnerWithStoreDTO(
        String ownerName,
        String ownerEmail,
        String ownerPassword,
        String ownerPhone,
        String subscriptionType,
        String storeName,
        String storeType,
        String storeAddress,
        Double storeLatitude,
        Double storeLongitude,
        String storeEmail,
        String storePhone,
        List<OpeningHoursDTO> openingHours
) {
    public OwnerWithStoreDTO {
        // Normaliser les emails en lowercase
        if (ownerEmail != null) {
            ownerEmail = ownerEmail.toLowerCase();
        }
        if (storeEmail != null) {
            storeEmail = storeEmail.toLowerCase();
        }
    }
}

