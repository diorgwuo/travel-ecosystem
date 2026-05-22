package com.travel.travelecosystem.infrastructure.persistence.repository;

import com.travel.travelecosystem.infrastructure.persistence.entity.FavoriteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {

    List<FavoriteEntity> findByUserId(Long userId);

    Optional<FavoriteEntity> findByUserIdAndPlaceId(Long userId, Long placeId);

    Optional<FavoriteEntity> findByUserIdAndExcursionId(Long userId, Long excursionId);

    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);

    boolean existsByUserIdAndExcursionId(Long userId, Long excursionId);

    void deleteByUserIdAndPlaceId(Long userId, Long placeId);

    void deleteByUserIdAndExcursionId(Long userId, Long excursionId);
}
