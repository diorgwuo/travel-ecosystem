package com.travel.travelecosystem.domain.service;

import com.travel.travelecosystem.infrastructure.persistence.entity.BookingEntity;
import com.travel.travelecosystem.infrastructure.persistence.entity.BookingStatus;
import com.travel.travelecosystem.infrastructure.persistence.entity.ExcursionEntity;
import com.travel.travelecosystem.infrastructure.persistence.entity.UserEntity;
import com.travel.travelecosystem.infrastructure.persistence.repository.BookingRepository;
import com.travel.travelecosystem.infrastructure.persistence.repository.ExcursionRepository;
import com.travel.travelecosystem.infrastructure.persistence.repository.UserJpaRepository;
import com.travel.travelecosystem.infrastructure.web.booking.dto.BookingListResponse;
import com.travel.travelecosystem.infrastructure.web.booking.dto.BookingRequest;
import com.travel.travelecosystem.infrastructure.web.booking.dto.BookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final List<BookingStatus> ACTIVE_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final ExcursionRepository excursionRepository;
    private final UserJpaRepository userJpaRepository;
    private final NotificationService notificationService;

    @Transactional
    public BookingResponse createBooking(Long touristId, BookingRequest request) {
        ExcursionEntity excursion = excursionRepository.findById(request.getExcursionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found"));
        if (!excursion.isPublished()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Excursion is not available for booking");
        }

        long reserved = bookingRepository.sumParticipantsByExcursionIdAndStatusIn(
                excursion.getId(), ACTIVE_STATUSES);
        if (reserved + request.getParticipantsCount() > excursion.getMaxParticipants()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough places available");
        }

        BookingEntity booking = BookingEntity.builder()
                .excursionId(excursion.getId())
                .touristId(touristId)
                .participantsCount(request.getParticipantsCount())
                .status(BookingStatus.PENDING)
                .build();
        BookingEntity saved = bookingRepository.save(booking);
        UserEntity tourist = userJpaRepository.findById(touristId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tourist not found"));
        String touristName = formatUserName(tourist);

        notificationService.createNotification(
                excursion.getOperatorId(),
                "NEW_BOOKING",
                "Новая заявка на бронирование",
                "Турист " + touristName + " забронировал экскурсию '" + excursion.getTitle() + "'");

        return toResponse(saved, excursion);
    }

    @Transactional(readOnly = true)
    public BookingListResponse getMyBookings(Long touristId, int limit, long offset) {
        List<BookingEntity> all = bookingRepository.findByTouristId(touristId);
        long total = all.size();
        int from = (int) Math.min(offset, total);
        int to = (int) Math.min(offset + limit, total);
        List<BookingResponse> items = all.subList(from, to).stream()
                .map(booking -> {
                    ExcursionEntity excursion = excursionRepository.findById(booking.getExcursionId())
                            .orElse(null);
                    return toResponse(booking, excursion);
                })
                .toList();
        return BookingListResponse.builder()
                .items(items)
                .limit(limit)
                .offset(offset)
                .totalSize(total)
                .build();
    }

    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, Long operatorId, String status) {
        BookingStatus newStatus = parseStatus(status);
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        ExcursionEntity excursion = excursionRepository.findById(booking.getExcursionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found"));

        if (!excursion.getOperatorId().equals(operatorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to modify this booking");
        }

        BookingStatus oldStatus = booking.getStatus();
        if (oldStatus == newStatus) {
            return toResponse(booking, excursion);
        }

        if (newStatus == BookingStatus.CONFIRMED) {
            if (oldStatus != BookingStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending bookings can be confirmed");
            }
            long reserved = bookingRepository.sumParticipantsByExcursionIdAndStatusIn(
                    excursion.getId(), List.of(BookingStatus.CONFIRMED));
            if (reserved + booking.getParticipantsCount() > excursion.getMaxParticipants()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough places available");
            }
            adjustBookingsCount(excursion, 1);
            booking.setStatus(BookingStatus.CONFIRMED);
            notificationService.createNotification(
                    booking.getTouristId(),
                    "BOOKING_CONFIRMED",
                    "Бронирование подтверждено",
                    "Ваше бронирование экскурсии '" + excursion.getTitle() + "' подтверждено");
        } else if (newStatus == BookingStatus.CANCELLED) {
            if (oldStatus == BookingStatus.CONFIRMED) {
                adjustBookingsCount(excursion, -1);
            }
            booking.setStatus(BookingStatus.CANCELLED);
            notificationService.createNotification(
                    booking.getTouristId(),
                    "BOOKING_REJECTED",
                    "Бронирование отклонено",
                    "Ваше бронирование экскурсии '" + excursion.getTitle() + "' отклонено");
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operators can only set CONFIRMED or CANCELLED status");
        }

        BookingEntity saved = bookingRepository.save(booking);
        return toResponse(saved, excursion);
    }

    @Transactional
    public BookingResponse cancelByTourist(Long bookingId, Long touristId) {
        BookingEntity booking = bookingRepository.findByIdAndTouristId(bookingId, touristId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        ExcursionEntity excursion = excursionRepository.findById(booking.getExcursionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return toResponse(booking, excursion);
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            adjustBookingsCount(excursion, -1);
        }
        booking.setStatus(BookingStatus.CANCELLED);
        BookingEntity saved = bookingRepository.save(booking);

        UserEntity tourist = userJpaRepository.findById(touristId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tourist not found"));
        String touristName = formatUserName(tourist);
        notificationService.createNotification(
                excursion.getOperatorId(),
                "BOOKING_CANCELLED_BY_TOURIST",
                "Бронирование отменено туристом",
                "Турист " + touristName + " отменил бронирование экскурсии '" + excursion.getTitle() + "'");

        return toResponse(saved, excursion);
    }

    private void adjustBookingsCount(ExcursionEntity excursion, int delta) {
        long current = excursion.getBookingsCount() == null ? 0 : excursion.getBookingsCount();
        excursion.setBookingsCount(Math.max(0, current + delta));
        excursionRepository.save(excursion);
    }

    private static BookingStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must not be blank");
        }
        try {
            return BookingStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }
    }

    private BookingResponse toResponse(BookingEntity booking, ExcursionEntity excursion) {
        return BookingResponse.builder()
                .id(booking.getId())
                .excursionId(booking.getExcursionId())
                .excursionTitle(excursion != null ? excursion.getTitle() : null)
                .excursionStartDate(excursion != null ? excursion.getStartDate() : null)
                .price(excursion != null ? excursion.getPrice() : null)
                .participantsCount(booking.getParticipantsCount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    private static String formatUserName(UserEntity user) {
        String first = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String last = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (first + " " + last).trim();
        if (!fullName.isEmpty()) {
            return fullName;
        }
        return user.getEmail();
    }
}
