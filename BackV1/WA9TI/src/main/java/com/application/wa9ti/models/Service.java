package com.application.wa9ti.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private int time;

    private double price;

    private String description;

    private boolean isActif;

    @Column(nullable = false)
    private String categoryName;

    @ManyToOne
    @JoinColumn(name = "store_id") // Définit la clé étrangère
    @JsonBackReference
    private Store store; // Propriétaire de la relation avec Store
}
