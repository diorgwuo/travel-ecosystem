package com.travel.travelecosystem.infrastructure.web.place.dto;

import com.travel.travelecosystem.domain.model.PlaceTheme;
import com.travel.travelecosystem.domain.model.PlaceTimeOfDay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceTagCatalogResponse {

    private List<PlaceTimeOfDay> timeOfDayTags;
    private List<PlaceTheme> themeTags;

    public static PlaceTagCatalogResponse defaults() {
        return new PlaceTagCatalogResponse(
                Arrays.asList(PlaceTimeOfDay.values()),
                Arrays.asList(PlaceTheme.values()));
    }
}
