package com.application.wa9ti.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "subscription", indexes = {
        @Index(name = "idx_subscription_end_date", columnList = "endDate"),
        @Index(name = "idx_subscription_status", columnList = "status")
})
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    public enum SubscriptionType {
        FREE,BASIC, PREMIUM, ENTERPRISE
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType type;

    public enum BillingType {
        FIXED,   // Facturation automatique le 5 du mois
        RECHARGE // Achat manuel de jours (30, 60, 120...)
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingType billingType;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate deletionDate;


    @PrePersist
    protected void onCreate() {
        if (this.endDate != null) {
            this.deletionDate = this.endDate.plusMonths(2);
        }
    }

    private LocalDate endDate;

    private SubscriptionType switchSubscriptionType;

    public enum SubscriptionStatus {
        ACTIVE,      // Paiement à jour le 1 du mois
        PENDING,     // En attente de paiement (pendant 15 jours )
        EXPIRED,     // Paiement non effectué après 15 jours
        CANCELED     // Abonnement annulé
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private boolean isNew = true;

}
