package com.application.wa9ti.dtos;

import java.time.LocalDateTime;

public record ReviewResponseDto(
        Long id,
        Long clientId,
        String clientName,
        String clientImage,
        Long storeId,
        float cleanliness,
        float hospitality,
        float serviceQuality,
        float valueForMoney,
        float experience,
        float rating, // Calculé automatiquement
        String comment,
        String storeResponse,
        LocalDateTime createdAt
) {}
