package com.application.wa9ti.models;

import com.application.wa9ti.enums.SubRole;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    @JsonManagedReference
    private Store store;

    @Column(nullable = false)
    private SubRole role; // Ex: "Manager", "Staff", etc.

    private String note;

    @OneToMany(mappedBy = "employeeStore", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<EmployeeSchedule> schedules; // Planning des jours et horaires

    @Column(nullable = false)
    private boolean isActive = true;

    @ElementCollection
    @CollectionTable(name = "employee_services", joinColumns = @JoinColumn(name = "employee_store_id"))
    @Column(name = "service_id")
    private List<Long> services; // Stocke les IDs des services

    @ElementCollection
    @CollectionTable(name = "employee_vacations", joinColumns = @JoinColumn(name = "employee_store_id"))
    private List<VacationSlot> vacations;

}
