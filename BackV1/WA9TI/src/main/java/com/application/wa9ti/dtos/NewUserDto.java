package com.application.wa9ti.dtos;

import com.application.wa9ti.enums.Role;

public record NewUserDto(
        String username,
        String email,
        String password,
        String phone,
        Role role
) {
    public NewUserDto {
        // Normaliser l'email en lowercase lors de la création du DTO
        if (email != null) {
            email = email.toLowerCase();
        }
    }
}

