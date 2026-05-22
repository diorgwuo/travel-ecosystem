package com.travel.travelecosystem.infrastructure.web.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {

    private Long id;
    private String type;
    private Long itemId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
