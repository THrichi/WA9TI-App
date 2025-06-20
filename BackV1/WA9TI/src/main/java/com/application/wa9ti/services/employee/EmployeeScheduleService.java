package com.application.wa9ti.services.employee;

import com.application.wa9ti.dtos.StoreEmployeeScheduleDto;

import java.util.List;

public interface EmployeeScheduleService {
    List<StoreEmployeeScheduleDto> getEmployeesWithSchedulesByStore(Long storeId);
    void updateWeeklySchedule(StoreEmployeeScheduleDto dto);
    StoreEmployeeScheduleDto getEmployeeScheduleByStore(Long employeeStoreId, Long storeId);

}

