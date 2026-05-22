package com.travel.travelecosystem.infrastructure.web.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponse {

    private List<NotificationResponse> items;
    private long unreadCount;
    private long totalSize;
}
