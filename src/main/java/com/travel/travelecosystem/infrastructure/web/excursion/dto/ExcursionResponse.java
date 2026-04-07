package com.travel.travelecosystem.infrastructure.web.excursion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcursionResponse {

    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private BigDecimal price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String meetingAddress;
    private Double latitude;
    private Double longitude;
    private Integer maxParticipants;

    @JsonProperty("isPublished")
    private boolean published;

    private Long operatorId;
    private String operatorName;
}
