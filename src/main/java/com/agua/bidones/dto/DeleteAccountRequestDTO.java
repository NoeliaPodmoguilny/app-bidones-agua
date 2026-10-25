package com.agua.bidones.dto;

import jakarta.validation.constraints.NotBlank;

/** Confirmación para eliminar la propia cuenta: la contraseña actual. */
public record DeleteAccountRequestDTO(
        @NotBlank(message = "Escribí tu contraseña para confirmar") String password
) {
}
