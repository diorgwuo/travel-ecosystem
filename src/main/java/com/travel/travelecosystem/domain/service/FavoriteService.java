package com.travel.travelecosystem.domain.service;

import com.travel.travelecosystem.infrastructure.persistence.entity.ExcursionEntity;
import com.travel.travelecosystem.infrastructure.persistence.entity.FavoriteEntity;
import com.travel.travelecosystem.infrastructure.persistence.entity.PlaceEntity;
import com.travel.travelecosystem.infrastructure.persistence.repository.ExcursionRepository;
import com.travel.travelecosystem.infrastructure.persistence.repository.FavoriteRepository;
import com.travel.travelecosystem.infrastructure.persistence.repository.PlaceRepository;
import com.travel.travelecosystem.infrastructure.web.favorite.dto.FavoriteListResponse;
import com.travel.travelecosystem.infrastructure.web.favorite.dto.FavoriteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PlaceRepository placeRepository;
    private final ExcursionRepository excursionRepository;

    @Transactional
    public FavoriteResponse addFavorite(Long userId, Long placeId, Long excursionId) {
        boolean hasPlace = placeId != null;
        boolean hasExcursion = excursionId != null;
        if (hasPlace == hasExcursion) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Specify exactly one of placeId or excursionId");
        }

        if (hasPlace) {
            if (favoriteRepository.existsByUserIdAndPlaceId(userId, placeId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Place already in favorites");
            }
            PlaceEntity place = placeRepository.findById(placeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));
            FavoriteEntity saved = favoriteRepository.save(FavoriteEntity.builder()
                    .userId(userId)
                    .placeId(placeId)
                    .build());
            adjustPlaceFavoritesCount(place, 1);
            return toPlaceResponse(saved, place);
        }

        if (favoriteRepository.existsByUserIdAndExcursionId(userId, excursionId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Excursion already in favorites");
        }
        ExcursionEntity excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found"));
        FavoriteEntity saved = favoriteRepository.save(FavoriteEntity.builder()
                .userId(userId)
                .excursionId(excursionId)
                .build());
        adjustExcursionFavoritesCount(excursion, 1);
        return toExcursionResponse(saved, excursion);
    }

    @Transactional
    public void removeFavorite(Long userId, Long placeId, Long excursionId) {
        boolean hasPlace = placeId != null;
        boolean hasExcursion = excursionId != null;
        if (hasPlace == hasExcursion) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Specify exactly one of placeId or excursionId");
        }

        if (hasPlace) {
            if (!favoriteRepository.existsByUserIdAndPlaceId(userId, placeId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found");
            }
            PlaceEntity place = placeRepository.findById(placeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));
            favoriteRepository.deleteByUserIdAndPlaceId(userId, placeId);
            adjustPlaceFavoritesCount(place, -1);
            return;
        }

        if (!favoriteRepository.existsByUserIdAndExcursionId(userId, excursionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found");
        }
        ExcursionEntity excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found"));
        favoriteRepository.deleteByUserIdAndExcursionId(userId, excursionId);
        adjustExcursionFavoritesCount(excursion, -1);
    }

    @Transactional(readOnly = true)
    public FavoriteListResponse getFavorites(Long userId, int limit, long offset) {
        List<FavoriteEntity> all = favoriteRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(FavoriteEntity::getCreatedAt).reversed())
                .toList();
        long total = all.size();
        int from = (int) Math.min(offset, total);
        int to = (int) Math.min(offset + limit, total);
        List<FavoriteResponse> items = all.subList(from, to).stream().map(this::toResponse).toList();
        return FavoriteListResponse.builder()
                .items(items)
                .limit(limit)
                .offset(offset)
                .totalSize(total)
                .build();
    }

    private FavoriteResponse toResponse(FavoriteEntity favorite) {
        if (favorite.getPlaceId() != null) {
            PlaceEntity place = placeRepository.findById(favorite.getPlaceId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));
            return toPlaceResponse(favorite, place);
        }
        ExcursionEntity excursion = excursionRepository.findById(favorite.getExcursionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found"));
        return toExcursionResponse(favorite, excursion);
    }

    private static FavoriteResponse toPlaceResponse(FavoriteEntity favorite, PlaceEntity place) {
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .type("PLACE")
                .itemId(place.getId())
                .name(place.getName())
                .description(place.getDescription())
                .createdAt(favorite.getCreatedAt())
                .build();
    }

    private static FavoriteResponse toExcursionResponse(FavoriteEntity favorite, ExcursionEntity excursion) {
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .type("EXCURSION")
                .itemId(excursion.getId())
                .name(excursion.getTitle())
                .description(excursion.getDescription())
                .createdAt(favorite.getCreatedAt())
                .build();
    }

    private void adjustPlaceFavoritesCount(PlaceEntity place, int delta) {
        long current = place.getFavoritesCount() == null ? 0 : place.getFavoritesCount();
        place.setFavoritesCount(Math.max(0, current + delta));
        placeRepository.save(place);
    }

    private void adjustExcursionFavoritesCount(ExcursionEntity excursion, int delta) {
        long current = excursion.getFavoritesCount() == null ? 0 : excursion.getFavoritesCount();
        excursion.setFavoritesCount(Math.max(0, current + delta));
        excursionRepository.save(excursion);
    }
}
