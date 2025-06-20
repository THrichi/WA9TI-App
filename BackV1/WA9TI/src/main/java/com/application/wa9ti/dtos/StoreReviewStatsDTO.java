package com.application.wa9ti.dtos;

public record StoreReviewStatsDTO(
        double averageRating,
        double hospitality,
        double cleanliness,
        double serviceQuality,
        double valueForMoney,
        double experience,
        long totalReviews
) {}
