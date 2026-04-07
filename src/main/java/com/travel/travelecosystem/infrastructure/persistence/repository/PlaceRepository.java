package com.travel.travelecosystem.infrastructure.persistence.repository;

import com.travel.travelecosystem.infrastructure.persistence.entity.PlaceCategory;
import com.travel.travelecosystem.infrastructure.persistence.entity.PlaceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PlaceRepository extends JpaRepository<PlaceEntity, Long> {

    Page<PlaceEntity> findByCategory(PlaceCategory category, Pageable pageable);

    @Query(
            value = "SELECT id FROM places WHERE ST_DWithin("
                    + "CAST(location AS geography), "
                    + "CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geography), "
                    + ":radiusMeters)",
            nativeQuery = true
    )
    List<Long> findNearbyPlaceIds(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusMeters") double radiusMeters);

    List<PlaceEntity> findByIdIn(Collection<Long> ids);
}
