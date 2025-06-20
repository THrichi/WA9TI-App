package com.application.wa9ti.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time") // Renommage de la colonne
    private LocalTime startTime;

    @Column(name = "end_time") // Renommage de la colonne
    private LocalTime endTime;

    // ✅ Relation avec EmployeeSchedule
    @ManyToOne
    @JoinColumn(name = "employee_schedule_id", nullable = false)
    private EmployeeSchedule schedule;
}

