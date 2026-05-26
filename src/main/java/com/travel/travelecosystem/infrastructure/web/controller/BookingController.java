package com.travel.travelecosystem.infrastructure.web.controller;

import com.travel.travelecosystem.domain.service.BookingService;
import com.travel.travelecosystem.infrastructure.web.booking.dto.BookingListResponse;
import com.travel.travelecosystem.infrastructure.web.booking.dto.BookingRequest;
import com.travel.travelecosystem.infrastructure.web.booking.dto.BookingResponse;
import com.travel.travelecosystem.infrastructure.web.booking.dto.BookingStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Бронирования", description = "Бронирование экскурсий")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать бронирование (турист)")
    public BookingResponse create(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Valid @RequestBody BookingRequest request) {
        return bookingService.createBooking(userId, request);
    }

    @GetMapping("/my")
    @Operation(summary = "Мои бронирования (турист)")
    public BookingListResponse myBookings(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return bookingService.getMyBookings(userId, limit, offset);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Изменить статус бронирования (туроператор)")
    public BookingResponse updateStatus(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody BookingStatusRequest request) {
        return bookingService.updateBookingStatus(id, userId, request.getStatus());
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Отменить бронирование (турист)")
    public BookingResponse cancelByTourist(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        return bookingService.cancelByTourist(id, userId);
    }
}
