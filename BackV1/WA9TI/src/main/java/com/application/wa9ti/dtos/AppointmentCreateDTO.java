package com.application.wa9ti.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentCreateDTO {
    private Long storeId;
    private Long employeeId;
    private Long clientId;
    private LocalDate date;
    private LocalTime startTime;
    private List<ServiceRequestDTO> services; // ✅ Gestion multiple des services
    private String clientNote;
    private String employeeNote;

}
