package com.travel.travelecosystem.infrastructure.persistence.repository;

import com.travel.travelecosystem.infrastructure.persistence.entity.ExcursionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ExcursionRepository extends JpaRepository<ExcursionEntity, Long> {

    @EntityGraph(attributePaths = "operator")
    Page<ExcursionEntity> findByOperatorId(Long operatorId, Pageable pageable);

    @EntityGraph(attributePaths = "operator")
    Page<ExcursionEntity> findByPublishedTrue(Pageable pageable);

    @EntityGraph(attributePaths = "operator")
    @Query(
            "SELECT e FROM ExcursionEntity e WHERE e.published = true "
                    + "AND e.startDate < :rangeEnd AND e.endDate > :rangeStart"
    )
    Page<ExcursionEntity> findPublishedByDateRange(
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    @EntityGraph(attributePaths = "operator")
    @Query("SELECT e FROM ExcursionEntity e WHERE e.id = :id")
    Optional<ExcursionEntity> findByIdWithOperator(@Param("id") Long id);
}
