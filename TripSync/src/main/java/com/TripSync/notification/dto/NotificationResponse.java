package com.TripSync.notification.dto;



import com.TripSync.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long tripId,
        NotificationType type,
        String title,
        String  message,
        boolean read,
        LocalDateTime createdAt
) {
}
