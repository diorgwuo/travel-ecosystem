package com.travel.travelecosystem.infrastructure.web.route.dto;

import com.travel.travelecosystem.domain.model.PlaceTheme;
import com.travel.travelecosystem.domain.model.PlaceTimeOfDay;
import com.travel.travelecosystem.domain.model.RoutePace;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteBuildOptionsResponse {

    private List<RoutePace> paceOptions;
    private List<PlaceTimeOfDay> timeOfDayOptions;
    private List<PlaceTheme> themeOptions;

    public static RouteBuildOptionsResponse defaults() {
        return new RouteBuildOptionsResponse(
                Arrays.asList(RoutePace.values()),
                Arrays.asList(PlaceTimeOfDay.values()),
                Arrays.asList(PlaceTheme.values()));
    }
}
