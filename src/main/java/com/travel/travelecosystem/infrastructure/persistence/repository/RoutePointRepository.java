package com.travel.travelecosystem.infrastructure.persistence.repository;

import com.travel.travelecosystem.infrastructure.persistence.entity.RoutePointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutePointRepository extends JpaRepository<RoutePointEntity, Long> {

    List<RoutePointEntity> findByRouteIdOrderByOrderIndexAsc(Long routeId);

    void deleteByRouteId(Long routeId);
}
