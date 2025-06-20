package com.application.wa9ti.dtos;

import com.application.wa9ti.models.EmployeeSchedule;
import com.application.wa9ti.models.SlotEmployee;

import java.util.List;

public record EmployeeScheduleDto(
        String day,
        List<SlotEmployeeDto> slots
) {
    public static EmployeeScheduleDto fromEntity(EmployeeSchedule schedule) {
        return new EmployeeScheduleDto(
                schedule.getDay(),
                schedule.getSlots().stream().map(SlotEmployeeDto::fromEntity).toList() // ✅ Conversion des slots
        );
    }
}