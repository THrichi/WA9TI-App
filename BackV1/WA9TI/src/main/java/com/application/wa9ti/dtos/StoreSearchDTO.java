package com.application.wa9ti.dtos;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreSearchDTO {
    private Long id;
    private String name;
    private String storeUrl;
    private String type;
    private String email;
    private String phone;
    private String address;
    private Double latitude;
    private Double longitude;
    private String image;
    private Double rating;
    private Long reviews;// Ajout du rating
}
