package com.application.wa9ti.dtos;

import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.Store;
import java.util.List;
import java.util.stream.Collectors;

public record OwnerDto(
        Long id,
        Long userId,
        String name,
        Long selectedStore,
        String selectedStoreName,
        String image,
        List<Long> stores
) {
    public static OwnerDto fromEntity(Owner owner) {
        if (owner == null) {
            return null;
        }

        return new OwnerDto(
                owner.getId(),
                owner.getUser().getId(),
                owner.getUser().getName(),
                (owner.getStores() != null && !owner.getStores().isEmpty()) ? owner.getStores().get(0).getId() : null,
                (owner.getStores() != null && !owner.getStores().isEmpty()) ? owner.getStores().get(0).getName() : null,
                owner.getImage(),
                owner.getStores() != null ? owner.getStores().stream().map(Store::getId).collect(Collectors.toList()) : List.of()
        );
    }
}
