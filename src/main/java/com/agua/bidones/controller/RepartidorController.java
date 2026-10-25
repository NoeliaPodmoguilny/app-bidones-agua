package com.agua.bidones.controller;

import com.agua.bidones.dto.*;
import com.agua.bidones.service.OrderService;
import com.agua.bidones.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repartidor")
@RequiredArgsConstructor
@Tag(name = "Repartidor", description = "Gestión de repartidores")
public class RepartidorController {

    private final UserService userService;
    private final OrderService orderService;

    @Operation(summary = "Registrar nuevo cliente", description = "Permite al repartidor registrar sus clientes")
    @PostMapping("/clientes")
    public ResponseEntity<UserResponseDTO> registerCliente(
            Authentication authentication,
            @Valid @RequestBody UserRequestDTO userRequest
    ) {
        String repartidorIdentifier = authentication.getName();
        UserResponseDTO response = userService.registerClienteByRepartidor(
                repartidorIdentifier,
                userRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Ver mis pedidos", description = "Ver pedidos del repartidor")
    @GetMapping("/orders")
    public ResponseEntity<List<RepartidorOrderResponseDTO>> getMyOrders(
            Authentication authentication,
            @RequestParam(name = "days", defaultValue = "30") int days
    ) {
        String repartidorIdentifier = authentication.getName();

        List<RepartidorOrderResponseDTO> response =
                orderService.getOrdersForRepartidor(repartidorIdentifier, days);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Entregar pedido", description = "Permite al repartidor entregar un pedido")
    @PatchMapping("/orders/{id}/deliver")
    public ResponseEntity<OrderResponseDTO> deliverOrder(Authentication authentication, @PathVariable Long id) {
        String repartidorIdentifier = authentication.getName();
        OrderResponseDTO response =
                orderService.deliverOrderByRepartidor(repartidorIdentifier, id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Rechazar pedido", description = "Permite al repartidor rechazar un pedido agregando el motivo")
    @PatchMapping("/orders/{id}/reject")
    public ResponseEntity<OrderResponseDTO> rejectOrder(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody RejectOrderRequestDTO request
    ) {
        String repartidorIdentifier = authentication.getName();
        OrderResponseDTO response =
                orderService.rejectOrderByRepartidor(repartidorIdentifier, id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Pedido en camino", description = "Permite al repartidor notificar que esta en camino")
    @PatchMapping("/orders/{id}/on-the-way")
    public ResponseEntity<OrderResponseDTO> onTheWayOrder(Authentication authentication, @PathVariable Long id) {
        String repartidorIdentifier = authentication.getName();
        OrderResponseDTO response =
                orderService.onTheWayOrderByRepartidor(repartidorIdentifier, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<UserResponseDTO>> myClientes(Authentication auth) {
        return ResponseEntity.ok(userService.getClientesOfRepartidor(auth.getName()));
    }

    @PatchMapping("/clientes/{id}/password")
    public ResponseEntity<Void> resetClientPassword(Authentication auth, @PathVariable Long id,
                                                    @Valid @RequestBody ResetPasswordRequestDTO request) {
        userService.resetClientPasswordByRepartidor(auth.getName(), id, request.newPassword());
        return ResponseEntity.noContent().build();
    }

}
