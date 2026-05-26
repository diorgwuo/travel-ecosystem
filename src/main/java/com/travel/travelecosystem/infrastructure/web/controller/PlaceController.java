package com.travel.travelecosystem.infrastructure.web.controller;

import com.travel.travelecosystem.domain.service.PlaceService;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceListResponse;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceRequest;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceResponse;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceTagCatalogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
@Tag(name = "Места", description = "Достопримечательности, рестораны, музеи")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/attractions")
    @Operation(summary = "Список достопримечательностей")
    public PlaceListResponse attractions(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return placeService.getByCategory("ATTRACTION", limit, offset);
    }

    @GetMapping("/restaurants")
    @Operation(summary = "Список ресторанов")
    public PlaceListResponse restaurants(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return placeService.getByCategory("RESTAURANT", limit, offset);
    }

    @GetMapping("/museums")
    @Operation(summary = "Список музеев")
    public PlaceListResponse museums(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return placeService.getByCategory("MUSEUM", limit, offset);
    }

    @GetMapping("/all")
    @Operation(summary = "Все места без фильтра по категории")
    public PlaceListResponse getAllPlaces(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return placeService.getAllPlaces(limit, offset);
    }

    @GetMapping("/tags")
    @Operation(summary = "Справочник тегов мест (время суток и тематика)")
    public PlaceTagCatalogResponse tagCatalog() {
        return PlaceTagCatalogResponse.defaults();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Место по идентификатору")
    public PlaceResponse getById(@PathVariable Long id) {
        return placeService.getById(id);
    }

    @GetMapping("/nearby")
    @Operation(summary = "Места рядом с точкой")
    public PlaceListResponse nearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double radius,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return placeService.findNearby(lat, lon, radius, limit, offset);
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск мест по названию или описанию")
    public PlaceListResponse search(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return placeService.search(query, category, limit, offset);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать место")
    public PlaceResponse create(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Valid @RequestBody PlaceRequest request) {
        return placeService.createPlace(request);
    }
}
