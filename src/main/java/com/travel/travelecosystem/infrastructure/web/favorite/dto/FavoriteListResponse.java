package com.travel.travelecosystem.infrastructure.web.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteListResponse {

    private List<FavoriteResponse> items;
    private int limit;
    private long offset;
    private long totalSize;
}
