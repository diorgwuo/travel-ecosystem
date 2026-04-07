package com.travel.travelecosystem.infrastructure.web.place.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceListResponse {

    private List<PlaceResponse> items;
    private int limit;
    private long offset;
    private long totalSize;
}
