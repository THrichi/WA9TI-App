package com.application.wa9ti.dtos;


import com.application.wa9ti.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id; // Ajoutez l'ID
    private String email;
    private String phone;
    private Role role;
}

