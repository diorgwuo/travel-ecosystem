package com.travel.travelecosystem.infrastructure.web.route.dto;

import com.travel.travelecosystem.domain.model.PlaceTimeOfDay;
import com.travel.travelecosystem.domain.model.RoutePace;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RouteAutoBuildRequest {

    @NotNull
    private RoutePace pace;

    @NotNull
    private PlaceTimeOfDay timeOfDay;

    @NotEmpty
    private List<String> themes;

    @Size(max = 255)
    private String title;
}
