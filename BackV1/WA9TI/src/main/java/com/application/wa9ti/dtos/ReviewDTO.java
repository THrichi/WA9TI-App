package com.application.wa9ti.dtos;


import com.application.wa9ti.models.Review;

public record ReviewDTO(
        Long clientId,
        Long storeId,
        float cleanliness,
        float hospitality,
        float serviceQuality,
        float valueForMoney,
        float experience,
        String comment
) {
    public static ReviewDTO fromEntity(Review review) {
        return new ReviewDTO(
                review.getClient().getId(),
                review.getStore().getId(),
                review.getCleanliness(),
                review.getHospitality(),
                review.getServiceQuality(),
                review.getValueForMoney(),
                review.getExperience(),
                review.getComment()
        );
    }
}
