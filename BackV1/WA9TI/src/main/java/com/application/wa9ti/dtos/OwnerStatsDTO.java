package com.application.wa9ti.dtos;

public record OwnerStatsDTO(
        Long ownerId,
        int numberOfStores,
        int numberOfAppointmentsThisMonth,
        int numberOfEmployees,
        int numberOfServices
) {}
