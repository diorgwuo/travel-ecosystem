package com.travel.travelecosystem.domain.service;

import com.travel.travelecosystem.infrastructure.persistence.entity.PlaceEntity;
import com.travel.travelecosystem.infrastructure.persistence.repository.PlaceRepository;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceListResponse;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceRequest;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private final PlaceRepository placeRepository;

    @Transactional(readOnly = true)
    public PlaceListResponse getByCategory(String category, int limit, long offset) {
        int page = (int) (offset / limit);
        Page<PlaceEntity> pageResult =
                placeRepository.findByCategoryAndPublishedTrue(category, PageRequest.of(page, limit));
        List<PlaceResponse> items = pageResult.getContent().stream().map(this::convertToDto).toList();
        return new PlaceListResponse(items, limit, offset, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PlaceListResponse getAllPlaces(int limit, long offset) {
        int page = (int) (offset / limit);
        Page<PlaceEntity> pageResult = placeRepository.findByPublishedTrue(PageRequest.of(page, limit));
        List<PlaceResponse> items = pageResult.getContent().stream().map(this::convertToDto).toList();
        return new PlaceListResponse(items, limit, offset, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PlaceResponse getById(Long id) {
        PlaceEntity entity = placeRepository.findByIdAndPublishedTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));
        return convertToDto(entity);
    }

    @Transactional
    public PlaceResponse createPlace(PlaceRequest request) {
        PlaceEntity entity = convertToEntity(request);
        PlaceEntity saved = placeRepository.save(entity);
        return convertToDto(saved);
    }

    @Transactional(readOnly = true)
    public PlaceListResponse findNearby(double lat, double lon, double radiusMeters, int limit, long offset) {
        List<PlaceEntity> ordered = placeRepository.findByPublishedTrue().stream()
                .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                .filter(p -> haversineMeters(lat, lon, p.getLatitude(), p.getLongitude()) <= radiusMeters)
                .sorted(Comparator.comparingDouble(
                        p -> haversineMeters(lat, lon, p.getLatitude(), p.getLongitude())))
                .toList();

        if (ordered.isEmpty()) {
            return new PlaceListResponse(List.of(), limit, offset, 0);
        }

        long total = ordered.size();
        int from = (int) Math.min(offset, total);
        int to = (int) Math.min(offset + limit, total);
        List<PlaceResponse> slice = ordered.subList(from, to).stream().map(this::convertToDto).toList();
        return new PlaceListResponse(slice, limit, offset, total);
    }

    @Transactional(readOnly = true)
    public PlaceListResponse search(String query, String category, int limit, long offset) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query must not be blank");
        }

        List<PlaceEntity> entities;
        if (category == null || category.isBlank()) {
            entities = placeRepository.searchByNameOrDescription(normalizedQuery);
        } else {
            entities = placeRepository.searchByNameOrDescriptionAndCategory(normalizedQuery, category.trim());
        }

        long total = entities.size();
        int from = (int) Math.min(offset, total);
        int to = (int) Math.min(offset + limit, total);
        List<PlaceResponse> items = entities.subList(from, to).stream().map(this::convertToDto).toList();
        return new PlaceListResponse(items, limit, offset, total);
    }

    public PlaceResponse convertToDto(PlaceEntity entity) {
        return new PlaceResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getAddress(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getImageUrl(),
                entity.getRating(),
                copyTagSet(entity.getTimeOfDayTags()),
                copyTagSet(entity.getThemeTags())
        );
    }

    public void applyTagsFromRequest(PlaceEntity entity, PlaceRequest request) {
        entity.setTimeOfDayTags(copyTagSet(request.getTimeOfDayTags()));
        entity.setThemeTags(copyTagSet(request.getThemeTags()));
    }

    private static <T> Set<T> copyTagSet(Set<T> source) {
        return source == null ? new HashSet<>() : new HashSet<>(source);
    }

    public PlaceEntity convertToEntity(PlaceRequest request) {
        PlaceEntity entity = PlaceEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .imageUrl(request.getImageUrl())
                .rating(request.getRating())
                .published(true)
                .build();
        applyTagsFromRequest(entity, request);
        return entity;
    }

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double sinHalfLat = Math.sin(dLat / 2);
        double sinHalfLon = Math.sin(dLon / 2);
        double a = sinHalfLat * sinHalfLat
                + Math.cos(radLat1) * Math.cos(radLat2) * sinHalfLon * sinHalfLon;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(Math.max(0.0, 1 - a)));
        return EARTH_RADIUS_METERS * c;
    }
}
