package com.application.wa9ti.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private int nbRdvTotal; // Nombre total de RDV pris dans ce store

    @Column(nullable = false)
    private int nbRdvCompleted; // Nombre de RDV actifs (COMPLETED)

    @Column(nullable = false)
    private int nbRdvActif; // Nombre de RDV actifs (CONFIRMED, PENDING)

    @Column(nullable = false)
    private int rdvAnnule; // Nombre de RDV annulés

    @Column(nullable = false)
    private int rdvNonRespecte; // Nombre de no-shows

    @Column(nullable = false)
    private boolean isBlackListed; // Si le client est bloqué pour ce store

    @Column(nullable = false)
    private boolean isNewClient; // Définit si ce client est considéré comme "nouveau" pour ce store

    private String note;
}
