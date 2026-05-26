package com.travel.travelecosystem.infrastructure.persistence.repository;

import com.travel.travelecosystem.infrastructure.persistence.entity.ComplaintEntity;
import com.travel.travelecosystem.infrastructure.persistence.entity.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<ComplaintEntity, Long> {

    Page<ComplaintEntity> findByStatus(ComplaintStatus status, Pageable pageable);
}
