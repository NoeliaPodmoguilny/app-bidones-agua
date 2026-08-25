package com.agua.bidones.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequestDTO (
    @NotBlank(message = "El usuario no puede estar vacío")
    String identifier,
    @NotBlank(message = "La contraseña no puede estar vacía")
    String password) {
}
