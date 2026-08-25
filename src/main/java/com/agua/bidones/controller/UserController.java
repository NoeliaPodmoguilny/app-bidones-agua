package com.agua.bidones.controller;

import com.agua.bidones.dto.ChangePasswordRequestDTO;
import com.agua.bidones.dto.PushTokenRequestDTO;
import com.agua.bidones.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de clientes")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Actualizar pushToken", description = "Actualiza el pushToken de notificaciones")
    @PatchMapping("/me/push-token")
    public ResponseEntity<Void> updatePushToken(
            Authentication authentication,
            @RequestBody PushTokenRequestDTO dto) {
        userService.updatePushToken(authentication.getName(), dto.pushToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualizar contraseña", description = "Permite cambiar/actualizar contraseña")
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequestDTO request
    ) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
