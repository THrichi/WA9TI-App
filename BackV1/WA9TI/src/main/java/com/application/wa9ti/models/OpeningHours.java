package com.application.wa9ti.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpeningHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String day; // Jour de la semaine

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "opening_hours_id") // Crée une clé étrangère dans Slot
    private List<Slot> slots;

    @ManyToOne
    @JoinColumn(name = "store_id")
    @JsonIgnore
    private Store store;
}