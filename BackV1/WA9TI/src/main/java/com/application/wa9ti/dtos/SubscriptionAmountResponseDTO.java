package com.application.wa9ti.dtos;
import java.time.LocalDate;

public record SubscriptionAmountResponseDTO(
        double monthlyPrice,
        double prorataAmount,
        double totalToPay,
        LocalDate nextBillingDate,
        String translationKey,  // Clé de traduction
        int daysRemaining, // Nombre de jours restants
        String currentMonth, // Mois actuel
        String billedMonth,
        String nextBillingMonth // Mois de la prochaine facturation
) {}
