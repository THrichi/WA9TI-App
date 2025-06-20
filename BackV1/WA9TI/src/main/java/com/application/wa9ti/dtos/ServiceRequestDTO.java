package com.application.wa9ti.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequestDTO {
    private Long serviceId;
    private int duration; // Durée du service en minutes
    private double price;
}