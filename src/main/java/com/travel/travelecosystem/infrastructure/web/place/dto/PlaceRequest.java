package com.travel.travelecosystem.infrastructure.web.place.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlaceRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 4000)
    private String description;

    @NotBlank
    private String category;

    @Size(max = 500)
    private String address;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @Size(max = 1024)
    private String imageUrl;

    private BigDecimal rating;
}
