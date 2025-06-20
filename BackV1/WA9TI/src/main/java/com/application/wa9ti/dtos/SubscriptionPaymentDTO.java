package com.application.wa9ti.dtos;

import com.application.wa9ti.models.Invoice;
import com.application.wa9ti.models.Subscription;

import java.time.LocalDate;

public record SubscriptionPaymentDTO(
        Long ownerId,
        Subscription.SubscriptionType type,
        Subscription.BillingType billingType,
        Invoice.PaymentMethod paymentMethod,
        double totalToPay,
        LocalDate nextBillingDate
) {}
