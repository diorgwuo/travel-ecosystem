package com.travel.travelecosystem.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "route_points",
        uniqueConstraints = @UniqueConstraint(columnNames = {"route_id", "order_index"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutePointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private RouteEntity route;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}
