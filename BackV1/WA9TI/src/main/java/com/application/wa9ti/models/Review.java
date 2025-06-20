package com.application.wa9ti.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "store_id"})
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    @JsonBackReference
    private Client client;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    @JsonIgnore
    private Store store;

    // Critères de notation
    @Column(nullable = false)
    @DecimalMin("0.0") @DecimalMax("5.0")
    private float cleanliness; // Propreté

    @Column(nullable = false)
    @DecimalMin("0.0") @DecimalMax("5.0")
    private float hospitality; // Accueil

    @Column(nullable = false)
    @DecimalMin("0.0") @DecimalMax("5.0")
    private float serviceQuality; // Qualité du service

    @Column(nullable = false)
    @DecimalMin("0.0") @DecimalMax("5.0")
    private float valueForMoney; // Rapport qualité/prix

    @Column(nullable = false)
    @DecimalMin("0.0") @DecimalMax("5.0")
    private float experience; // Expérience globale

    // Note moyenne calculée
    @Column(nullable = false)
    private float rating;

    @Column(length = 500)
    private String comment;

    @Column(length = 500)
    private String storeResponse;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    protected void calculateRating() {
        this.rating = (cleanliness + hospitality + serviceQuality + valueForMoney + experience) / 5;
        this.createdAt = LocalDateTime.now();
    }
}
