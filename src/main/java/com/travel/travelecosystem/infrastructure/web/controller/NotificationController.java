package com.travel.travelecosystem.infrastructure.web.controller;

import com.travel.travelecosystem.domain.service.NotificationService;
import com.travel.travelecosystem.infrastructure.web.notification.dto.NotificationListResponse;
import com.travel.travelecosystem.infrastructure.web.notification.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Уведомления", description = "Уведомления пользователя")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Мои уведомления")
    public NotificationListResponse list(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return notificationService.getMyNotifications(userId, limit, offset);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Отметить уведомление как прочитанное")
    public NotificationResponse markRead(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        return notificationService.markAsRead(id, userId);
    }

    @PutMapping("/read-all")
    @Operation(summary = "Отметить все уведомления как прочитанные")
    public void markAllRead(@Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        notificationService.markAllAsRead(userId);
    }
}
