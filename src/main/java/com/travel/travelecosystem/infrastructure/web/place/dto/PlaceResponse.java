package com.travel.travelecosystem.infrastructure.web.place.dto;

import com.travel.travelecosystem.infrastructure.persistence.entity.PlaceCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceResponse {

    private Long id;
    private String name;
    private String description;
    private PlaceCategory category;
    private String address;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private BigDecimal rating;
}
