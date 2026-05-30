package com.travel.travelecosystem.infrastructure.persistence.repository;

import com.travel.travelecosystem.domain.model.PlaceTheme;
import com.travel.travelecosystem.domain.model.PlaceTimeOfDay;
import com.travel.travelecosystem.infrastructure.persistence.entity.PlaceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PlaceRepository extends JpaRepository<PlaceEntity, Long> {

    Page<PlaceEntity> findByCategoryAndPublishedTrue(String category, Pageable pageable);

    Page<PlaceEntity> findByPublishedTrue(Pageable pageable);

    java.util.Optional<PlaceEntity> findByIdAndPublishedTrue(Long id);

    List<PlaceEntity> findByPublishedTrue();

    List<PlaceEntity> findByIdIn(Collection<Long> ids);

    @Query(
            value = "SELECT * FROM places " +
                    "WHERE is_published = true AND (name ILIKE CONCAT('%', :query, '%') " +
                    "OR description ILIKE CONCAT('%', :query, '%'))",
            nativeQuery = true
    )
    List<PlaceEntity> searchByNameOrDescription(@Param("query") String query);

    @Query(
            value = "SELECT * FROM places " +
                    "WHERE is_published = true AND (name ILIKE CONCAT('%', :query, '%') " +
                    "OR description ILIKE CONCAT('%', :query, '%')) " +
                    "AND LOWER(category) = LOWER(:category)",
            nativeQuery = true
    )
    List<PlaceEntity> searchByNameOrDescriptionAndCategory(
            @Param("query") String query,
            @Param("category") String category);

    @Query("""
            SELECT DISTINCT p FROM PlaceEntity p
            JOIN p.themeTags theme
            WHERE p.published = true
            AND :timeOfDay MEMBER OF p.timeOfDayTags
            AND theme IN :themes
            """)
    List<PlaceEntity> findPublishedByTimeOfDayAndThemes(
            @Param("timeOfDay") PlaceTimeOfDay timeOfDay,
            @Param("themes") Collection<PlaceTheme> themes);
}
