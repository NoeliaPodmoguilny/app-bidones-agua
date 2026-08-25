package com.agua.bidones.dto;

import com.agua.bidones.enums.NotificationType;

import java.time.Instant;

public record NotificationResponseDTO(
        Long id,
        String userId,
        NotificationType type,
        Long orderId,
        String title,
        String message,
        Boolean read,
        Instant createdAt
) {
}
