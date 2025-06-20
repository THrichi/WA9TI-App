package com.application.wa9ti.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, name = "start_time") // Renomme la colonne dans la base
    private LocalTime startTime;

    @Column(nullable = false, name = "end_time") // Renomme la colonne dans la base
    private LocalTime endTime;

    @Column(nullable = false)
    private double price; // Utilise Float pour éviter float(53)

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    private String clientName;

    private String clientEmail;

    private String clientPhone;

    private String clientNote;

    private String employeeNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // Enum pour représenter le statut
    public enum Status {
        CONFIRMED,
        PENDING,
        COMPLETED,
        MISSED
    }
}
