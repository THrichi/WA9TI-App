package com.application.wa9ti.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/*@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentDto {
    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private double price;
    private Long storeId;
    private String storeName;
    private Long employeeId;
    private String employeeName;
    private Long serviceId;
    private String serviceName;
    private String clientNote;
    private String employeeNote;
    private String status;
}
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentDto {
    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private double price;
    private Long storeId;
    private String storeName;
    private Long employeeId;
    private String employeeName;
    private Long serviceId;
    private String serviceName;
    private String clientNote;
    private String employeeNote;
    private String status;
    private Long clientId;    // Ajout du client ID
    private String clientName; // Ajout du nom du client
    private String clientImage; // Ajout de l'image du client
    private String clientEmail;
    private String clientPhone;
    private int reservationsCount;
}
