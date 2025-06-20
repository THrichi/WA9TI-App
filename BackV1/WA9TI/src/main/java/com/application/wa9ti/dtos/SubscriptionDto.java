package com.application.wa9ti.dtos;

import com.application.wa9ti.configuration.SubscriptionConfig;
import com.application.wa9ti.models.Invoice;
import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Store;
import com.application.wa9ti.models.Subscription;
import com.application.wa9ti.models.Subscription.SubscriptionType;
import com.application.wa9ti.models.Subscription.BillingType;
import com.application.wa9ti.models.Subscription.SubscriptionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record SubscriptionDto(
        Long id,
        SubscriptionType type,
        BillingType billingType,
        LocalDate startDate,
        LocalDate endDate,
        SubscriptionType switchSubscriptionType,
        SubscriptionStatus status,
        int rdvCount,
        double price,
        InvoiceDto currentInvoice,
        boolean isNew
) {
    public static SubscriptionDto fromEntity(Subscription subscription, Long storeId) {
        assert subscription.getOwner() != null;
        return new SubscriptionDto(
                subscription.getId(),
                subscription.getType(),
                subscription.getBillingType(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getSwitchSubscriptionType(),
                subscription.getStatus(),
                Optional.of(subscription)
                        .map(Subscription::getOwner)
                        .map(Owner::getStores)
                        .flatMap(stores -> stores.stream()
                                .filter(store -> store.getId().equals(storeId)) // Filtrer par storeId
                                .findFirst() // Trouver le bon magasin
                        )
                        .map(Store::getRdvCount) // Récupérer rdvCount
                        .map(Number::intValue) // Conversion en int
                        .orElse(0),
                SubscriptionConfig.getPrice(subscription.getType()),
                subscription.getOwner().getCurrentInvoice() != null
                        ? InvoiceDto.fromEntity(subscription.getOwner().getCurrentInvoice())
                        : null,
                subscription.isNew()

        );
    }

}

