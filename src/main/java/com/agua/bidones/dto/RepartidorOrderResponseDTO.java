package com.agua.bidones.dto;

import com.agua.bidones.enums.Status;

import java.time.Instant;

public record RepartidorOrderResponseDTO(
        Long id,
        Status status,
        Integer quantity,
        String address,
        String rejectionReason,
        Instant createdAt,
        String clientName,
        String clientIdentifier
) {
}
