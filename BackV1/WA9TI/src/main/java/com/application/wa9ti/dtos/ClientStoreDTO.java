package com.application.wa9ti.dtos;

import lombok.Builder;

@Builder
public record ClientStoreDTO(
        Long clientId,
        String name,
        String email,
        String phone,
        String image,
        int nbRdvTotal,
        int nbRdvCompleted,
        int nbRdvActif,
        int rdvAnnule,
        int rdvNonRespecte,
        boolean isBlackListed,
        boolean isNewClient,
        String clientNote
) {}
