package com.agua.bidones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDTO(
        @NotBlank @Size(min = 6, max = 72, message = "La contraseña debe tener al menos 6 caracteres")
        String newPassword) {}
