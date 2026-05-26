package com.travel.travelecosystem.infrastructure.web.controller;

import com.travel.travelecosystem.domain.service.RouteService;
import com.travel.travelecosystem.infrastructure.web.route.dto.RouteAutoBuildRequest;
import com.travel.travelecosystem.infrastructure.web.route.dto.RouteBuildOptionsResponse;
import com.travel.travelecosystem.infrastructure.web.route.dto.RouteListResponse;
import com.travel.travelecosystem.infrastructure.web.route.dto.RouteRequest;
import com.travel.travelecosystem.infrastructure.web.route.dto.RouteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Tag(name = "Маршруты", description = "Пользовательские маршруты по местам")
public class RouteController {

    private final RouteService routeService;

    @GetMapping("/build-options")
    @Operation(summary = "Справочник параметров автоконструктора маршрута")
    public RouteBuildOptionsResponse buildOptions() {
        return RouteBuildOptionsResponse.defaults();
    }

    @PostMapping("/auto-build")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Автоматически собрать маршрут по тегам и темпу")
    public RouteResponse autoBuild(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Valid @RequestBody RouteAutoBuildRequest request) {
        return routeService.autoBuildRoute(userId, request);
    }

    @GetMapping
    @Operation(summary = "Мои маршруты")
    public RouteListResponse list(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return routeService.getByUser(userId, limit, offset);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Маршрут по id")
    public RouteResponse getById(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        return routeService.getById(id, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать маршрут")
    public RouteResponse create(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Valid @RequestBody RouteRequest request) {
        return routeService.create(userId, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить маршрут")
    public RouteResponse update(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody RouteRequest request) {
        return routeService.update(id, userId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить маршрут")
    public void delete(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        routeService.delete(id, userId);
    }
}
