package com.travel.travelecosystem.infrastructure.web.excursion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExcursionRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 8000)
    private String description;

    @NotNull
    @Positive
    private Integer duration;

    @NotNull
    private BigDecimal price;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;

    @Size(max = 500)
    private String meetingAddress;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    @Positive
    private Integer maxParticipants;

    @JsonProperty("isPublished")
    private boolean published;
}
