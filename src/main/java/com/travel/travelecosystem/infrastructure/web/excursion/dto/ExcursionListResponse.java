package com.travel.travelecosystem.infrastructure.web.excursion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcursionListResponse {

    private List<ExcursionResponse> items;
    private int limit;
    private long offset;
    private long totalSize;
}
