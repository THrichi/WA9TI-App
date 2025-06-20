package com.application.wa9ti.dtos;

import com.application.wa9ti.models.Slot;
import com.application.wa9ti.models.SlotEmployee;

import java.time.LocalTime;

public record SlotEmployeeDto(
                              LocalTime startTime,
                              LocalTime endTime
) {
    public static SlotEmployeeDto fromEntity(SlotEmployee slot) {
        return new SlotEmployeeDto(
                slot.getStartTime(),
                slot.getEndTime()
        );
    }
}