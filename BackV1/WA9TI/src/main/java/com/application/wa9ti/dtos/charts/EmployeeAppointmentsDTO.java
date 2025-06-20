package com.application.wa9ti.dtos.charts;

public record EmployeeAppointmentsDTO(String employeeName, String photo, Long appointmentCount) {

    public static EmployeeAppointmentsDTO fromEntity(Object[] result) {
        return new EmployeeAppointmentsDTO(
                (String) result[0],  // Nom de l’employé
                (String) result[1],  // Photo de l’employé
                (Long) result[2]     // Nombre total de rendez-vous
        );
    }
}