package com.travel.travelecosystem.infrastructure.persistence.repository;

import com.travel.travelecosystem.infrastructure.persistence.entity.RouteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<RouteEntity, Long> {

    @EntityGraph(attributePaths = "points")
    Page<RouteEntity> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "points")
    Optional<RouteEntity> findByIdAndUserId(Long id, Long userId);
}
