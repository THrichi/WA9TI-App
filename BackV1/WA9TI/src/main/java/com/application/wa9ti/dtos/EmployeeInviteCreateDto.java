package com.application.wa9ti.dtos;

import com.application.wa9ti.enums.SubRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record EmployeeInviteCreateDto(
        String email,
        String username,
        String password,
        Long storeId,
        SubRole subRole
) {
    public EmployeeInviteCreateDto {
        // Normaliser l'email en lowercase lors de la création du DTO
        if (email != null) {
            email = email.toLowerCase();
        }
    }
}

