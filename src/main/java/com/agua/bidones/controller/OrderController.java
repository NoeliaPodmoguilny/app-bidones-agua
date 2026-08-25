package com.agua.bidones.controller;


import com.agua.bidones.dto.CreateOrderRequestDTO;
import com.agua.bidones.dto.OrderResponseDTO;
import com.agua.bidones.service.OrderService;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gestión de pedidos")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Ver mis pedidos", description = "Ver pedidos del cliente")
    @GetMapping("/me")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders(Authentication authentication) {
        if(authentication==null){
            throw new RuntimeException("Authentication is null");
        }
        String identifier = authentication.getName();
        List<OrderResponseDTO> orders = orderService.getOrdersByUserIdentifier(identifier);
        return ResponseEntity.ok(orders);
    }
    @Operation(summary = "Crear nuevo pedido", description = "Crear nuevo pedido como cliente")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(Authentication authentication,
                                                        @Valid @RequestBody CreateOrderRequestDTO request) {
        if(authentication==null){
            throw new RuntimeException("Authentication is null");
        }
        String identifier = authentication.getName();
        OrderResponseDTO response = orderService.createOrder(identifier, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Eliminar pedido", description = "Elimina/cancela un pedido del cliente logueado, sólo si está pendiente")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id){
        orderService.deleteOrder(id);
        return ResponseEntity.status(HttpStatus.OK).body("Pedido eliminado");
    }
}
