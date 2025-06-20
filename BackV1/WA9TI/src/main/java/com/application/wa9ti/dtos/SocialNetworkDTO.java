package com.application.wa9ti.dtos;

import com.application.wa9ti.enums.SocialPlatform;
import com.application.wa9ti.models.SocialNetwork;

public record SocialNetworkDTO(
        Long id,
        SocialPlatform platform,
        String url,
        String icon
) {
    public static SocialNetworkDTO fromEntity(SocialNetwork socialNetwork) {
        return new SocialNetworkDTO(
                socialNetwork.getId(),
                socialNetwork.getPlatform(),
                socialNetwork.getUrl(),
                socialNetwork.getIcon()
        );
    }
}