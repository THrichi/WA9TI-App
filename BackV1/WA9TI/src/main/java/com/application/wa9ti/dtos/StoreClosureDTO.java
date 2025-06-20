package com.application.wa9ti.dtos;

import com.application.wa9ti.models.StoreClosure;

import java.time.LocalDate;
import java.time.LocalTime;

public record StoreClosureDTO(
        Long id,
        Long storeId,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime startTime,
        LocalTime endTime
) {
    public static StoreClosureDTO fromEntity(StoreClosure closure) {
        return new StoreClosureDTO(
                closure.getId(),
                closure.getStore().getId(),
                closure.getStartDate(),
                closure.getEndDate(),
                closure.getStartTime(),
                closure.getEndTime()
        );
    }
}

