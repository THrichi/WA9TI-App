package com.application.wa9ti.dtos;

import java.util.List;

public record StoreCreateDTO(String name,
                             String storeUrl,
                             String type,
                             String email,
                             String phone,
                             String address,
                             Double latitude,
                             Double longitude,
                             Long ownerId,
                             List<OpeningHoursDTO> openingHours
) {}