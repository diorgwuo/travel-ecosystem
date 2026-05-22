package com.travel.travelecosystem.infrastructure.persistence.repository;

import com.travel.travelecosystem.infrastructure.persistence.entity.BookingEntity;
import com.travel.travelecosystem.infrastructure.persistence.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByTouristId(Long touristId);

    List<BookingEntity> findByExcursionId(Long excursionId);

    Optional<BookingEntity> findByIdAndTouristId(Long id, Long touristId);

    long countByExcursionId(Long excursionId);

    @Query(
            "SELECT COALESCE(SUM(b.participantsCount), 0) FROM BookingEntity b "
                    + "WHERE b.excursionId = :excursionId AND b.status IN :statuses"
    )
    long sumParticipantsByExcursionIdAndStatusIn(
            @Param("excursionId") Long excursionId,
            @Param("statuses") List<BookingStatus> statuses);
}
