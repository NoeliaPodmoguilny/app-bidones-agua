package com.agua.bidones.controller;

import com.agua.bidones.dto.*;
import com.agua.bidones.service.AdminService;
import com.agua.bidones.service.OrderService;
import com.agua.bidones.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Gestion de panel admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final OrderService orderService;

    @PostMapping("/newAdmin1")
    @Operation(summary = "Registrar nuevo admin", description = "Guarda un nuevo admin")
    public ResponseEntity<UserResponseDTO> registerAdmin(@Valid @RequestBody UserRequestDTO userRequest) {
        UserResponseDTO response = adminService.registerAdmin(userRequest);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/repartidores")
    @Operation(summary = "Registrar nuevo repartidor", description = "Guarda un nuevo repartidor")
    public ResponseEntity<UserResponseDTO> registerRepartidor(
            Authentication authentication,
            @Valid @RequestBody UserRequestDTO userRequest) {
        String adminIdentifier = authentication.getName();
        UserResponseDTO response = userService.registerRepartidor(adminIdentifier, userRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/orders")
    @Operation(summary = "Reporte de pedidos", description = "Filtra pedidos por estado, cliente, repartidor o fecha")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersReport(
            Authentication authentication,
            @ModelAttribute OrderFilterDTO filter) {
        String authIdentifier = authentication.getName();
        return ResponseEntity.ok(orderService.getOrdersReport(authIdentifier, filter));
    }

    @GetMapping("/report/clientes")
    @Operation(summary = "Reporte de clientes", description = "Muestra la lista de clientes registrados")
    public ResponseEntity<List<UserReportResponseDTO>> getClientsReport(Authentication authentication) {
        String authIdentifier = authentication.getName();
        return ResponseEntity.ok(adminService.getClientsReport(authIdentifier));
    }

    @GetMapping("/report/repartidores")
    @Operation(summary = "Reporte de repartidores", description = "Muestra la lista de repartidores registrados")
    public ResponseEntity<List<UserReportResponseDTO>> getRepartidoresReport(Authentication authentication) {
        String authIdentifier = authentication.getName();
        return ResponseEntity.ok(adminService.getRepartidoresReport(authIdentifier));
    }

}
