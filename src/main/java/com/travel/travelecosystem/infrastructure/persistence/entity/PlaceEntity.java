package com.travel.travelecosystem.infrastructure.persistence.entity;

import com.travel.travelecosystem.domain.model.PlaceTheme;
import com.travel.travelecosystem.domain.model.PlaceTimeOfDay;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "places")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, length = 64)
    private String category;

    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean published = true;

    @Column(name = "favorites_count")
    @Builder.Default
    private Long favoritesCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "place_time_of_day_tags", joinColumns = @JoinColumn(name = "place_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "time_of_day", nullable = false)
    @Builder.Default
    private Set<PlaceTimeOfDay> timeOfDayTags = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "place_theme_tags", joinColumns = @JoinColumn(name = "place_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false)
    @Builder.Default
    private Set<PlaceTheme> themeTags = new HashSet<>();
}
