package com.agua.bidones.controller;

import com.agua.bidones.dto.NotificationResponseDTO;
import com.agua.bidones.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Gestión de las notificaciones y pushTokens")
public class NotificationController {

    private final NotificationService notificationService;


    @Operation(summary = "Obtener mis notificaciones", description = "Devuelve las notificaciones del usuario logueado, sea cliente, repartidor o admin")
    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications(Authentication authentication) {
        String identifier = authentication.getName();
        return ResponseEntity.ok(
                notificationService.getNotificationsForUser(identifier)
        );
    }

    @Operation(summary = "Cantidad de notificaciones sin leer", description = "Devuelve solo el número, sin traer la lista")
    @GetMapping("/unread-count")
    public ResponseEntity<java.util.Map<String, Long>> unreadCount(Authentication authentication) {
        return ResponseEntity.ok(java.util.Map.of("count", notificationService.countUnread(authentication.getName())));
    }

    @Operation(summary = "Marcar notificaciones como leidas", description = "Marca como leídas todas las notificaciones del usuario logueado")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String identifier = authentication.getName();
        notificationService.markAllAsRead(identifier);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Marcar una notificacion como leidas", description = "Marca como leída una notificación puntual")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markOneAsRead(Authentication authentication, @PathVariable Long id) {
        String identifier = authentication.getName();
        notificationService.markOneAsRead(identifier, id);
        return ResponseEntity.noContent().build();
    }

}
