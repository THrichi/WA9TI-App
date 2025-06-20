package com.application.wa9ti.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreRoleDto {
    private Long storeId; // ID du magasin
    private String storeName; // Nom du magasin
    private String role; // Rôle dans ce magasin
}
