package com.application.wa9ti.configuration;

import com.application.wa9ti.models.Subscription;

import java.util.Map;

public class SubscriptionConfig {

    public static final Integer FREE_APPOINTEMENTS =  200;
    public static final Map<Subscription.SubscriptionType, Double> PRICES = Map.of(
            Subscription.SubscriptionType.FREE, 0.0,
            Subscription.SubscriptionType.BASIC, 250.0,
            Subscription.SubscriptionType.ENTERPRISE, 450.0,
            Subscription.SubscriptionType.PREMIUM, 950.0
    );

    public static double getPrice(Subscription.SubscriptionType type) {
        return PRICES.getOrDefault(type, 0.0);
    }

    // Définition des limitations par abonnement
    public static final Map<Subscription.SubscriptionType, Integer> MAX_EMPLOYEES = Map.of(
            Subscription.SubscriptionType.FREE, 1,
            Subscription.SubscriptionType.BASIC, 2,
            Subscription.SubscriptionType.ENTERPRISE, 5,
            Subscription.SubscriptionType.PREMIUM, 10
    );

    public static final Map<Subscription.SubscriptionType, Integer> MAX_SERVICES = Map.of(
            Subscription.SubscriptionType.FREE, 5,
            Subscription.SubscriptionType.BASIC, 15,
            Subscription.SubscriptionType.ENTERPRISE, 30,
            Subscription.SubscriptionType.PREMIUM, 50
    );

    public static final Map<Subscription.SubscriptionType, Integer> MAX_STORES = Map.of(
            Subscription.SubscriptionType.FREE, 1,
            Subscription.SubscriptionType.BASIC, 1,
            Subscription.SubscriptionType.ENTERPRISE, 2,
            Subscription.SubscriptionType.PREMIUM, 5
    );

    public static int getMaxEmployees(Subscription.SubscriptionType type) {
        return MAX_EMPLOYEES.getOrDefault(type, 0);
    }

    public static int getMaxServices(Subscription.SubscriptionType type) {
        return MAX_SERVICES.getOrDefault(type, 0);
    }

    public static int getMaxStores(Subscription.SubscriptionType type) {
        return MAX_STORES.getOrDefault(type, 0);
    }
}
