package com.application.wa9ti.dtos;

import com.application.wa9ti.models.VacationSlot;

import java.util.List;

public record AppointmentEmployeeDTO(
        Long id,
        String name,
        String image,
        List<AppointmentDto> appointments,
        List<VacationSlot> vacations,
        List<Long> services
) {}

