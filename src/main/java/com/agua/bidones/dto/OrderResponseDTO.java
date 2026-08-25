package com.agua.bidones.dto;

import com.agua.bidones.enums.Status;

import java.time.Instant;

public record OrderResponseDTO(
        Long id,
        Status status,
        Integer quantity,
        String address,
        String rejectionReason,
        Instant createdAt,
        String clienteName,
        String repartidorName
) {
}
