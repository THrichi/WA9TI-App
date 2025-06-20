package com.application.wa9ti.dtos;

import com.application.wa9ti.models.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentValidationDTO(
        Long id,
        Long clientId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String clientName,
        String clientPhone,
        String clientEmail,
        String clientImage,
        String clientNote,
        int clientNbRdvTotal,
        int clientNbRdvAnnule,
        int clientNbRdvActif,
        int clientNbRdvNonRespecte,
        String serviceName,
        Double servicePrice,
        int serviceTime,
        String storeName,
        String employeeName,
        String employeeImage,
        String employeeNote,
        Appointment.Status status
) {}
