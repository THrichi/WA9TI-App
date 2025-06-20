package com.application.wa9ti.dtos;

import com.application.wa9ti.models.OpeningHours;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public record OpeningHoursDTO(
        Long id,
        String day,
        List<SlotDTO> slots
) {
    public static OpeningHoursDTO fromEntity(OpeningHours openingHours) {
        return new OpeningHoursDTO(
                openingHours.getId(),
                openingHours.getDay(),
                openingHours.getSlots().stream().map(SlotDTO::fromEntity).toList()
        );
    }
}