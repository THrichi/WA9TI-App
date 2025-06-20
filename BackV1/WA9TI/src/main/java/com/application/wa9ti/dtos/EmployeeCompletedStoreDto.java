package com.application.wa9ti.dtos;

import com.application.wa9ti.enums.SubRole;
import com.application.wa9ti.models.Employee;
import com.application.wa9ti.models.EmployeeSchedule;
import com.application.wa9ti.models.EmployeeStore;
import com.application.wa9ti.models.VacationSlot;

import java.util.List;
import java.util.stream.Collectors;

public record EmployeeCompletedStoreDto(
        Long id,
        Long storeId,
        Long employeeId,
        Long userId,
        String employeeName,
        String employeeEmail,
        String employeePhone,
        String employeeImage,
        String employeeNote,
        SubRole role,
        List<String> serviceNames,
        List<VacationSlot> vacations,
        List<EmployeeScheduleDto> schedules,
        Boolean active
) {
    public static EmployeeCompletedStoreDto fromEmployee(Long storeId, Employee employee, EmployeeStore employeeStore, List<String> serviceNames) {
        return new EmployeeCompletedStoreDto(
                employeeStore.getId(),
                storeId,
                employee.getId(),
                employee.getUser().getId(),
                employee.getUser().getName(),
                employee.getUser().getEmail(),
                employee.getUser().getPhone(),
                employee.getImage(),
                employeeStore.getNote(),
                employeeStore.getRole(),
                serviceNames,
                employeeStore.getVacations(),
                employeeStore.getSchedules().stream()
                        .map(EmployeeScheduleDto::fromEntity) // ✅ Conversion en DTO
                        .collect(Collectors.toList()),
                employeeStore.isActive()
        );
    }
}