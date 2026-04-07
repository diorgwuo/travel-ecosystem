package com.travel.travelecosystem.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "excursions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcursionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    /** Длительность в минутах */
    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "meeting_point", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point meetingPoint;

    @Column(name = "meeting_address")
    private String meetingAddress;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    private UserEntity operator;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
