package com.travel.travelecosystem.infrastructure.web.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingListResponse {

    private List<BookingResponse> items;
    private int limit;
    private long offset;
    private long totalSize;
}
