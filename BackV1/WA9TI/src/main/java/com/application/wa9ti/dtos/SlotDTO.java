package com.application.wa9ti.dtos;

import com.application.wa9ti.models.Slot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

public record SlotDTO(
        LocalTime startTime,
        LocalTime endTime
) {
    public static SlotDTO fromEntity(Slot slot) {
        return new SlotDTO(
                slot.getStartTime(),
                slot.getEndTime()
        );
    }
}