package com.application.wa9ti.dtos;

import com.application.wa9ti.enums.SubRole;

public record NewEmployeeDto(
        String name,
        String email,
        String password,
        SubRole role,
        String note
) {
    public NewEmployeeDto {
        // Normaliser l'email en lowercase lors de la création du DTO
        if (email != null) {
            email = email.toLowerCase();
        }
    }
}

