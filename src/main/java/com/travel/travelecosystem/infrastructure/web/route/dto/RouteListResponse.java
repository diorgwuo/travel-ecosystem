package com.travel.travelecosystem.infrastructure.web.route.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteListResponse {

    private List<RouteResponse> items;
    private int limit;
    private long offset;
    private long totalSize;
}
