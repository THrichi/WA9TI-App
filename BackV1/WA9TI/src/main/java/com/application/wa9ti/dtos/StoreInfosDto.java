package com.application.wa9ti.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreInfosDto {
    private String storeName;
    private String storeType;
    private String storeAddress;
    private Double storeLatitude;
    private Double storeLongitude;
    private String storeEmail;
    private String storePhone;
    private String storeDescription;
    private List<String> storeSeo;
    private List<SocialNetworkDTO> socialNetworks;
}
