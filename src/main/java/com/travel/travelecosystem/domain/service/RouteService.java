package com.travel.travelecosystem.domain.service;

import com.travel.travelecosystem.domain.model.PlaceTheme;
import com.travel.travelecosystem.infrastructure.persistence.entity.PlaceEntity;
import com.travel.travelecosystem.infrastructure.persistence.entity.RouteEntity;
import com.travel.travelecosystem.infrastructure.persistence.entity.RoutePointEntity;
import com.travel.travelecosystem.infrastructure.persistence.repository.PlaceRepository;
import com.travel.travelecosystem.infrastructure.persistence.repository.RouteRepository;
import com.travel.travelecosystem.infrastructure.web.route.dto.RouteAutoBuildRequest;
import com.travel.travelecosystem.infrastructure.web.route.dto.RouteListResponse;
import com.travel.travelecosystem.infrastructure.web.route.dto.RoutePointResponse;
import com.travel.travelecosystem.infrastructure.web.route.dto.RouteRequest;
import com.travel.travelecosystem.infrastructure.web.route.dto.RouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private static final double EARTH_RADIUS_METERS = 6371000.0;
    /** Скорость «пешком» между точками, км/ч. */
    private static final double WALKING_SPEED_KMH = 5.0;
    /** Базовое время на посещение одной точки, минуты. */
    private static final long MINUTES_PER_STOP = 30;

    private final RouteRepository routeRepository;
    private final PlaceRepository placeRepository;

    @Transactional(readOnly = true)
    public RouteListResponse getByUser(Long userId, int limit, long offset) {
        int page = (int) (offset / Math.max(limit, 1));
        Page<RouteEntity> pageResult = routeRepository.findByUserId(userId, PageRequest.of(page, limit));
        List<RouteResponse> items = pageResult.getContent().stream().map(this::toResponse).toList();
        return new RouteListResponse(items, limit, offset, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public RouteResponse getById(Long id, Long userId) {
        RouteEntity route = routeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));
        return toResponse(route);
    }

    @Transactional
    public RouteResponse autoBuildRoute(Long userId, RouteAutoBuildRequest request) {
        int stopCount = request.getPace().getStopCount();
        Set<PlaceTheme> themes = parseThemes(request.getThemes());

        List<PlaceEntity> candidates = new ArrayList<>(
                placeRepository.findPublishedByTimeOfDayAndThemes(request.getTimeOfDay(), themes));

        if (candidates.size() < stopCount) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Недостаточно мест для построения маршрута");
        }

        Collections.shuffle(candidates);
        List<Long> placeIds = candidates.stream()
                .limit(stopCount)
                .map(PlaceEntity::getId)
                .toList();

        RouteRequest routeRequest = new RouteRequest();
        routeRequest.setTitle(resolveAutoBuildTitle(request));
        routeRequest.setPlaceIds(placeIds);
        return create(userId, routeRequest);
    }

    @Transactional
    public RouteResponse create(Long userId, RouteRequest request) {
        assertAllPlacesExist(request.getPlaceIds());
        RouteEntity route = RouteEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .userId(userId)
                .build();
        attachPoints(route, request.getPlaceIds());
        RouteEntity saved = routeRepository.save(route);
        return toResponse(saved);
    }

    @Transactional
    public RouteResponse update(Long id, Long userId, RouteRequest request) {
        RouteEntity route = routeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));
        assertAllPlacesExist(request.getPlaceIds());
        route.setTitle(request.getTitle());
        route.setDescription(request.getDescription());
        route.getPoints().clear();
        attachPoints(route, request.getPlaceIds());
        RouteEntity saved = routeRepository.save(route);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        RouteEntity route = routeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));
        routeRepository.delete(route);
    }

    public double calculateTotalDistance(List<RoutePointResponse> orderedPoints) {
        if (orderedPoints == null || orderedPoints.size() < 2) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = 0; i < orderedPoints.size() - 1; i++) {
            RoutePointResponse a = orderedPoints.get(i);
            RoutePointResponse b = orderedPoints.get(i + 1);
            if (a.getLatitude() == null || a.getLongitude() == null
                    || b.getLatitude() == null || b.getLongitude() == null) {
                continue;
            }
            sum += haversineMeters(
                    a.getLatitude(), a.getLongitude(),
                    b.getLatitude(), b.getLongitude());
        }
        return sum;
    }

    public long calculateTotalDuration(List<RoutePointResponse> orderedPoints) {
        double distanceMeters = calculateTotalDistance(orderedPoints);
        double distanceKm = distanceMeters / 1000.0;
        long travelMinutes = (long) Math.ceil(distanceKm / WALKING_SPEED_KMH * 60.0);
        long stopMinutes = orderedPoints == null ? 0 : (long) orderedPoints.size() * MINUTES_PER_STOP;
        long total = travelMinutes + stopMinutes;
        return Math.max(total, 1L);
    }

    private static Set<PlaceTheme> parseThemes(List<String> themes) {
        Set<PlaceTheme> parsed = new LinkedHashSet<>();
        for (String theme : themes) {
            String normalized = theme.trim().toUpperCase();
            if (normalized.isEmpty()) {
                continue;
            }
            try {
                parsed.add(PlaceTheme.valueOf(normalized));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Неизвестная тема: " + theme);
            }
        }
        if (parsed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "themes must not be empty");
        }
        return parsed;
    }

    private static String resolveAutoBuildTitle(RouteAutoBuildRequest request) {
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            return request.getTitle().trim();
        }
        return "Маршрут: " + String.join(", ", request.getThemes());
    }

    private void assertAllPlacesExist(List<Long> placeIds) {
        if (placeIds == null || placeIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "placeIds must not be empty");
        }
        LinkedHashSet<Long> unique = new LinkedHashSet<>(placeIds);
        List<PlaceEntity> found = placeRepository.findAllById(unique);
        if (found.size() != unique.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more places do not exist");
        }
    }

    private void attachPoints(RouteEntity route, List<Long> placeIds) {
        int index = 0;
        for (Long placeId : placeIds) {
            RoutePointEntity point = RoutePointEntity.builder()
                    .route(route)
                    .placeId(placeId)
                    .orderIndex(index++)
                    .build();
            route.getPoints().add(point);
        }
    }

    private RouteResponse toResponse(RouteEntity route) {
        List<Long> ids = route.getPoints().stream().map(RoutePointEntity::getPlaceId).toList();
        Map<Long, PlaceEntity> placesById = placeRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(PlaceEntity::getId, Function.identity()));

        List<RoutePointResponse> points = route.getPoints().stream()
                .map(rp -> toPointResponse(rp, placesById.get(rp.getPlaceId())))
                .toList();

        double distance = calculateTotalDistance(points);
        long duration = calculateTotalDuration(points);

        return new RouteResponse(
                route.getId(),
                route.getTitle(),
                route.getDescription(),
                route.getUserId(),
                points,
                duration,
                distance,
                route.getCreatedAt()
        );
    }

    private static RoutePointResponse toPointResponse(RoutePointEntity rp, PlaceEntity place) {
        String name = place != null ? place.getName() : null;
        Double lat = place != null ? place.getLatitude() : null;
        Double lon = place != null ? place.getLongitude() : null;
        return new RoutePointResponse(rp.getPlaceId(), name, lat, lon, rp.getOrderIndex());
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
