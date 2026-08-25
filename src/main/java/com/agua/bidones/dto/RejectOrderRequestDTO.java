package com.agua.bidones.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectOrderRequestDTO(
        @NotBlank(message = "El motivo de rechazo es obligatorio")
        String rejectionReason
) {
}
