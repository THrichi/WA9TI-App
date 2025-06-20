package com.application.wa9ti.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Informations sur l'Owner (client)
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Column(nullable = false)
    private String ownerName; // Nom du client

    @Column(nullable = false)
    private String ownerAddress; // Adresse du client

    @Column(nullable = true)
    private String ownerICE; // ICE du client (si entreprise)

    // Informations sur l'abonnement
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Subscription.SubscriptionType subscriptionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Subscription.BillingType billingType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Double amountHT; // Montant Hors Taxes

    @Column(nullable = false)
    private Double amountTVA; // Montant de la TVA

    @Column(nullable = false)
    private Double amountTTC; // Montant TTC (HT + TVA)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private LocalDateTime invoiceDate = LocalDateTime.now();

    @Column(nullable = false)
    private String pdfPath;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentMethod {
        CARD, PAYPAL, BANK_TRANSFER, CASH
    }
}
