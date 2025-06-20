package com.application.wa9ti.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreClosure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    @JsonBackReference
    private Store store; // Lien avec le magasin

    @Column(nullable = false)
    private LocalDate startDate; // Date de fermeture

    private LocalDate endDate; // Date de fermeture

    private LocalTime startTime; // Heure de début (optionnel)
    private LocalTime endTime;   // Heure de fin (optionnel)
}
