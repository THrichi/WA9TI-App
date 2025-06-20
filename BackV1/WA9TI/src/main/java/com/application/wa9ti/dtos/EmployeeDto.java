package com.application.wa9ti.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Long id; // ID de l'employé
    private String name; // Nom de l'employé
    private String email; // Email depuis l'entité User
    private String image; // Image de l'employé
    private String note; // Note associée
    private List<StoreRoleDto> stores; // Liste des magasins et des rôles associés
}
