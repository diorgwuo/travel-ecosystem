package com.travel.travelecosystem.domain.service;

import com.travel.travelecosystem.infrastructure.persistence.entity.ExcursionEntity;
import com.travel.travelecosystem.infrastructure.persistence.entity.UserEntity;
import com.travel.travelecosystem.infrastructure.persistence.repository.ExcursionRepository;
import com.travel.travelecosystem.infrastructure.persistence.repository.UserJpaRepository;
import com.travel.travelecosystem.infrastructure.web.excursion.dto.ExcursionListResponse;
import com.travel.travelecosystem.infrastructure.web.excursion.dto.ExcursionRequest;
import com.travel.travelecosystem.infrastructure.web.excursion.dto.ExcursionResponse;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcursionService {

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    private final ExcursionRepository excursionRepository;
    private final UserJpaRepository userJpaRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public ExcursionListResponse getAll(
            int limit, long offset, java.time.LocalDateTime rangeStart, java.time.LocalDateTime rangeEnd) {
        boolean hasStart = rangeStart != null;
        boolean hasEnd = rangeEnd != null;
        if (hasStart != hasEnd) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide both query params from and to, or neither");
        }

        int page = (int) (offset / Math.max(limit, 1));
        Page<ExcursionEntity> result;
        if (hasStart) {
            if (!rangeStart.isBefore(rangeEnd)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be before to");
            }
            result = excursionRepository.findPublishedByDateRange(rangeStart, rangeEnd, PageRequest.of(page, limit));
        } else {
            result = excursionRepository.findByPublishedTrue(PageRequest.of(page, limit));
        }
        return toListResponse(result, limit, offset);
    }

    @Transactional(readOnly = true)
    public ExcursionListResponse getByOperator(Long operatorId, int limit, long offset) {
        int page = (int) (offset / Math.max(limit, 1));
        Page<ExcursionEntity> result =
                excursionRepository.findByOperatorId(operatorId, PageRequest.of(page, limit));
        return toListResponse(result, limit, offset);
    }

    @Transactional
    public ExcursionResponse getById(Long id, Long viewerUserId) {
        ExcursionEntity entity = excursionRepository.findByIdWithOperator(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found"));
        if (!entity.isPublished()) {
            if (viewerUserId == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found");
            }
            UserEntity viewer = userJpaRepository.findById(viewerUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
            if (viewer.getRole() != UserEntity.UserRole.ROLE_ADMIN
                    && !entity.getOperatorId().equals(viewerUserId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found");
            }
        }
        entity.incrementViewsCount();
        excursionRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void incrementViews(Long id) {
        ExcursionEntity entity = excursionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found"));
        if (!entity.isPublished()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found");
        }
        entity.incrementViewsCount();
        excursionRepository.save(entity);
    }

    @Transactional
    public ExcursionResponse create(Long operatorUserId, ExcursionRequest request) {
        validateDates(request);
        Point meeting = GEOMETRY_FACTORY.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
        meeting.setSRID(4326);

        ExcursionEntity entity = ExcursionEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .duration(request.getDuration())
                .price(request.getPrice())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .meetingPoint(meeting)
                .meetingAddress(request.getMeetingAddress())
                .maxParticipants(request.getMaxParticipants())
                .published(request.isPublished())
                .operatorId(operatorUserId)
                .build();

        ExcursionEntity saved = excursionRepository.save(entity);
        return excursionRepository.findByIdWithOperator(saved.getId())
                .map(this::toResponse)
                .orElseGet(() -> toResponse(saved));
    }

    @Transactional
    public ExcursionResponse update(Long id, Long userId, ExcursionRequest request) {
        ExcursionEntity entity = excursionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found"));
        boolean wasPublished = entity.isPublished();
        assertOwnerOrAdmin(userId, entity);
        UserEntity actor = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        validateDates(request);

        Point meeting = GEOMETRY_FACTORY.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
        meeting.setSRID(4326);

        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setDuration(request.getDuration());
        entity.setPrice(request.getPrice());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setMeetingPoint(meeting);
        entity.setMeetingAddress(request.getMeetingAddress());
        entity.setMaxParticipants(request.getMaxParticipants());
        entity.setPublished(request.isPublished());

        excursionRepository.save(entity);
        if (actor.getRole() == UserEntity.UserRole.ROLE_ADMIN && wasPublished != request.isPublished()) {
            if (request.isPublished()) {
                notificationService.createNotification(
                        entity.getOperatorId(),
                        "EXCURSION_PUBLISHED",
                        "Экскурсия опубликована",
                        "Ваша экскурсия '" + entity.getTitle() + "' опубликована администратором");
            } else {
                notificationService.createNotification(
                        entity.getOperatorId(),
                        "EXCURSION_REJECTED",
                        "Экскурсия отклонена",
                        "Ваша экскурсия '" + entity.getTitle() + "' отклонена администратором");
            }
        }
        return excursionRepository.findByIdWithOperator(id).map(this::toResponse).orElseThrow();
    }

    @Transactional
    public void delete(Long id, Long userId) {
        ExcursionEntity entity = excursionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found"));
        assertOwnerOrAdmin(userId, entity);
        excursionRepository.delete(entity);
    }

    @Transactional
    public ExcursionResponse publish(Long id, Long userId) {
        ExcursionEntity entity = excursionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found"));
        assertOwnerOrAdmin(userId, entity);
        UserEntity actor = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        boolean wasPublished = entity.isPublished();
        entity.setPublished(true);
        excursionRepository.save(entity);
        if (!wasPublished && actor.getRole() == UserEntity.UserRole.ROLE_ADMIN) {
            notificationService.createNotification(
                    entity.getOperatorId(),
                    "EXCURSION_PUBLISHED",
                    "Экскурсия опубликована",
                    "Ваша экскурсия '" + entity.getTitle() + "' опубликована администратором");
        }
        return excursionRepository.findByIdWithOperator(id).map(this::toResponse).orElseThrow();
    }

    @Transactional
    public ExcursionResponse setPublicationStatusByAdmin(Long id, Long adminUserId, boolean publish) {
        ExcursionEntity entity = excursionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Excursion not found"));
        UserEntity actor = userJpaRepository.findById(adminUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (actor.getRole() != UserEntity.UserRole.ROLE_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        boolean wasPublished = entity.isPublished();
        entity.setPublished(publish);
        excursionRepository.save(entity);
        if (wasPublished != publish) {
            if (publish) {
                notificationService.createNotification(
                        entity.getOperatorId(),
                        "EXCURSION_PUBLISHED",
                        "Экскурсия опубликована",
                        "Ваша экскурсия '" + entity.getTitle() + "' опубликована администратором");
            } else {
                notificationService.createNotification(
                        entity.getOperatorId(),
                        "EXCURSION_REJECTED",
                        "Экскурсия отклонена",
                        "Ваша экскурсия '" + entity.getTitle() + "' отклонена администратором");
            }
        }
        return excursionRepository.findByIdWithOperator(id).map(this::toResponse).orElseThrow();
    }

    private void assertOwnerOrAdmin(Long userId, ExcursionEntity entity) {
        UserEntity actor = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (actor.getRole() == UserEntity.UserRole.ROLE_ADMIN) {
            return;
        }
        if (!entity.getOperatorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to modify this excursion");
        }
    }

    private static void validateDates(ExcursionRequest request) {
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must be before endDate");
        }
    }

    private ExcursionListResponse toListResponse(Page<ExcursionEntity> page, int limit, long offset) {
        List<ExcursionResponse> items = page.getContent().stream().map(this::toResponse).collect(Collectors.toList());
        return new ExcursionListResponse(items, limit, offset, page.getTotalElements());
    }

    private ExcursionResponse toResponse(ExcursionEntity entity) {
        Point mp = entity.getMeetingPoint();
        Double lat = mp != null ? mp.getY() : null;
        Double lon = mp != null ? mp.getX() : null;
        String operatorName;
        if (entity.getOperator() != null) {
            operatorName = trimJoin(entity.getOperator().getFirstName(), entity.getOperator().getLastName());
        } else {
            operatorName = userJpaRepository.findById(entity.getOperatorId())
                    .map(u -> trimJoin(u.getFirstName(), u.getLastName()))
                    .orElse(null);
        }
        return new ExcursionResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getDuration(),
                entity.getPrice(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getMeetingAddress(),
                lat,
                lon,
                entity.getMaxParticipants(),
                entity.isPublished(),
                entity.getOperatorId(),
                operatorName
        );
    }

    private static String trimJoin(String first, String last) {
        String f = first != null ? first.trim() : "";
        String l = last != null ? last.trim() : "";
        String joined = (f + " " + l).trim();
        return joined.isEmpty() ? null : joined;
    }
}
