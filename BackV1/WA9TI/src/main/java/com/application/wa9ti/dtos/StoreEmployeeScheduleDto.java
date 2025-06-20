package com.application.wa9ti.dtos;

import java.util.List;

public record StoreEmployeeScheduleDto(
        Long employeeStoreId,
        Long employeeId,
        String employeeName,
        String employeeImage,
        String employeeRole,
        List<EmployeeScheduleDto> schedules
) {}