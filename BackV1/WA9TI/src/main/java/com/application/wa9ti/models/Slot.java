package com.application.wa9ti.models;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time") // Renommage de la colonne
    private LocalTime startTime;

    @Column(name = "end_time") // Renommage de la colonne
    private LocalTime endTime;
}

