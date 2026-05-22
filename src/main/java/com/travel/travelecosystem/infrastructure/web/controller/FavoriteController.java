package com.travel.travelecosystem.infrastructure.web.controller;

import com.travel.travelecosystem.domain.service.FavoriteService;
import com.travel.travelecosystem.infrastructure.web.favorite.dto.FavoriteListResponse;
import com.travel.travelecosystem.infrastructure.web.favorite.dto.FavoriteRequest;
import com.travel.travelecosystem.infrastructure.web.favorite.dto.FavoriteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Избранное", description = "Избранные места и экскурсии")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить в избранное")
    public FavoriteResponse add(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @RequestBody FavoriteRequest request) {
        return favoriteService.addFavorite(userId, request.getPlaceId(), request.getExcursionId());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить из избранного")
    public void remove(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) Long placeId,
            @RequestParam(required = false) Long excursionId) {
        favoriteService.removeFavorite(userId, placeId, excursionId);
    }

    @GetMapping
    @Operation(summary = "Список избранного")
    public FavoriteListResponse list(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return favoriteService.getFavorites(userId, limit, offset);
    }
}
