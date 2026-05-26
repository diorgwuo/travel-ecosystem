package com.travel.travelecosystem.infrastructure.web.place.dto;

import com.travel.travelecosystem.domain.model.PlaceTheme;
import com.travel.travelecosystem.domain.model.PlaceTimeOfDay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String address;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private BigDecimal rating;
    private Set<PlaceTimeOfDay> timeOfDayTags;
    private Set<PlaceTheme> themeTags;
}
