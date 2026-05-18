package com.travel.travelecosystem.domain.service;

import com.travel.travelecosystem.infrastructure.persistence.entity.PlaceCategory;
import com.travel.travelecosystem.infrastructure.persistence.entity.PlaceEntity;
import com.travel.travelecosystem.infrastructure.persistence.repository.PlaceRepository;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceListResponse;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceRequest;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceResponse;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    private final PlaceRepository placeRepository;

    @Transactional(readOnly = true)
    public PlaceListResponse getByCategory(PlaceCategory category, int limit, long offset) {
        int page = (int) (offset / limit);
        Page<PlaceEntity> pageResult = placeRepository.findByCategory(category, PageRequest.of(page, limit));
        List<PlaceResponse> items = pageResult.getContent().stream().map(this::convertToDto).toList();
        return new PlaceListResponse(items, limit, offset, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PlaceResponse getById(Long id) {
        PlaceEntity entity = placeRepository.findById(id)
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
        List<Long> ids = placeRepository.findNearbyPlaceIds(lat, lon, radiusMeters);
        if (ids.isEmpty()) {
            return new PlaceListResponse(List.of(), limit, offset, 0);
        }
        List<PlaceEntity> all = placeRepository.findByIdIn(ids);
        Map<Long, PlaceEntity> byId = all.stream().collect(Collectors.toMap(PlaceEntity::getId, e -> e));
        List<PlaceEntity> ordered = ids.stream().map(byId::get).filter(e -> e != null).toList();

        long total = ordered.size();
        int from = (int) Math.min(offset, total);
        int to = (int) Math.min(offset + limit, total);
        List<PlaceResponse> slice = ordered.subList(from, to).stream().map(this::convertToDto).toList();
        return new PlaceListResponse(slice, limit, offset, total);
    }

    @Transactional(readOnly = true)
    public List<PlaceResponse> search(String query, String category) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query must not be blank");
        }

        List<PlaceEntity> entities;
        if (category == null || category.isBlank()) {
            entities = placeRepository.searchByNameOrDescription(normalizedQuery);
        } else {
            String normalizedCategory = normalizeCategory(category);
            entities = placeRepository.searchByNameOrDescriptionAndCategory(normalizedQuery, normalizedCategory);
        }

        return entities.stream().map(this::convertToDto).toList();
    }

    private String normalizeCategory(String category) {
        String normalizedCategory = category.trim();
        if (normalizedCategory.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category must not be blank");
        }

        String upperCategory = normalizedCategory.toUpperCase(Locale.ROOT);
        if ("ATTRACTION".equals(upperCategory) || "RESTAURANT".equals(upperCategory)) {
            return upperCategory;
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Category must be one of: attraction, restaurant");
    }

    public PlaceResponse convertToDto(PlaceEntity entity) {
        Point loc = entity.getLocation();
        Double lon = loc != null ? loc.getX() : null;
        Double lat = loc != null ? loc.getY() : null;
        return new PlaceResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getAddress(),
                lat,
                lon,
                entity.getImageUrl(),
                entity.getRating()
        );
    }

    public PlaceEntity convertToEntity(PlaceRequest request) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
        point.setSRID(4326);
        return PlaceEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .address(request.getAddress())
                .location(point)
                .imageUrl(request.getImageUrl())
                .rating(request.getRating())
                .build();
    }
}
