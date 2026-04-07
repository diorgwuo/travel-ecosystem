package com.travel.travelecosystem.infrastructure.web.route.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {

    private Long id;
    private String title;
    private String description;
    private Long userId;
    private List<RoutePointResponse> points;
    /** Примерная длительность маршрута в минутах (переходы ~5 км/ч + время на точках). */
    private Long totalDuration;
    /** Суммарная длина переходов между точками в метрах. */
    private Double totalDistance;
    private LocalDateTime createdAt;
}
