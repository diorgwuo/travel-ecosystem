package com.travel.travelecosystem.infrastructure.web.booking.dto;

import com.travel.travelecosystem.infrastructure.persistence.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long excursionId;
    private String excursionTitle;
    private LocalDateTime excursionStartDate;
    private BigDecimal price;
    private Integer participantsCount;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
