package com.application.wa9ti.dtos;

import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Subscription;

public record OwnerRegistrationDto(
        String name,
        String email,
        String phone,
        String password,
        Subscription.SubscriptionType subscriptionType
) {}
