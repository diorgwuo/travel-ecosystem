package com.travel.travelecosystem.infrastructure.web.route.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RouteRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 4000)
    private String description;

    @NotEmpty
    private List<Long> placeIds;
}
