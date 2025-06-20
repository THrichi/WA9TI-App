package com.application.wa9ti.dtos;

import com.application.wa9ti.models.Subscription;

public record CalculateSubscriptionAmountDTO(
        Long ownerId,
        Subscription.SubscriptionType subscriptionType
) {
}
