package com.travel.travelecosystem.infrastructure.web.route.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePointResponse {

    private Long placeId;
    private String placeName;
    private Double latitude;
    private Double longitude;
    private Integer orderIndex;
}
