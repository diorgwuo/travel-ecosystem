package com.travel.travelecosystem.domain.service;

import com.travel.travelecosystem.infrastructure.persistence.entity.NotificationEntity;
import com.travel.travelecosystem.infrastructure.persistence.repository.NotificationRepository;
import com.travel.travelecosystem.infrastructure.web.notification.dto.NotificationListResponse;
import com.travel.travelecosystem.infrastructure.web.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponse createNotification(Long userId, String type, String title, String message) {
        NotificationEntity entity = NotificationEntity.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .build();
        NotificationEntity saved = notificationRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public NotificationListResponse getMyNotifications(Long userId, int limit, long offset) {
        List<NotificationEntity> all = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        long total = all.size();
        int from = (int) Math.min(offset, total);
        int to = (int) Math.min(offset + limit, total);
        List<NotificationResponse> items = all.subList(from, to).stream().map(this::toResponse).toList();
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return new NotificationListResponse(items, unreadCount, total);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        NotificationEntity entity = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to modify this notification");
        }
        entity.setRead(true);
        return toResponse(notificationRepository.save(entity));
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<NotificationEntity> unread =
                notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (NotificationEntity entity : unread) {
            entity.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    private NotificationResponse toResponse(NotificationEntity entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
