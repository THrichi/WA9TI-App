package com.application.wa9ti.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotsDTO {

    private Long storeId;
    private String storeName;

    private LocalDate weekStartDate; // Date du début de la semaine chargée
    private LocalDate weekEndDate;   // Date de fin de la semaine chargée

    private List<DayAvailability> days; // Liste des jours avec les créneaux

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayAvailability {
        private LocalDate date;
        private String dayOfWeek;
        private List<SlotAvailability> slots;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotAvailability {
        private LocalTime startTime;
        private LocalTime endTime;
        private List<EmployeeAvailability> availableEmployees;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeAvailability {
        private Long employeeId;
        private String employeeName;
        private String employeeImage;
        private boolean isSpecialized;
    }
}
