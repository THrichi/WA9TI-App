package com.application.wa9ti.dtos;


public record LoginDto(
        String email,
        String password
) {
    public LoginDto {
        // Normaliser l'email en lowercase lors de la création du DTO
        if (email != null) {
            email = email.toLowerCase();
        }
    }
}
