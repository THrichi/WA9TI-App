package com.application.wa9ti.dtos;

import com.application.wa9ti.enums.SubRole;
import com.application.wa9ti.models.Employee;
import com.application.wa9ti.models.EmployeeStore;
import com.application.wa9ti.models.VacationSlot;

import java.util.List;

public record EmployeeStoreDto(
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
        List<Long> serviceIds,
        List<VacationSlot> vacations,
        Boolean active
) {
    public static EmployeeStoreDto fromEmployee(Long storeId,Employee employee, EmployeeStore employeeStore) {
        return new EmployeeStoreDto(
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
                employeeStore.getServices(),
                employeeStore.getVacations(),
                employeeStore.isActive()
        );
    }
}