package com.application.wa9ti.dtos.charts;

public record AverageAppointmentsDTO(Double averageAppointments) {

    public static AverageAppointmentsDTO fromEntity(Object[] result) {
        return new AverageAppointmentsDTO((Double) result[0]);
    }
}