package com.agua.bidones.controller;

import com.agua.bidones.dto.*;
import com.agua.bidones.security.LoginAttemptService;
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
    private final LoginAttemptService attemptService;

    @PostMapping("/newAdmin1")
    @Operation(summary = "Registrar nuevo admin", description = "Guarda un nuevo admin")
    public ResponseEntity<UserResponseDTO> registerAdmin(
            jakarta.servlet.http.HttpServletRequest request,
            @Valid @RequestBody UserRequestDTO userRequest) {
        attemptService.checkRegistrationAllowed(LoginAttemptService.clientIp(request));
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

    @GetMapping("/summary")
    @Operation(summary = "Resumen del día", description = "Cantidades de hoy por estado y pedidos abiertos de días anteriores (solo números)")
    public ResponseEntity<AdminSummaryDTO> getSummary(Authentication authentication) {
        return ResponseEntity.ok(orderService.getAdminSummary(authentication.getName()));
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

    @GetMapping("/report/repartidores/inactive")
    @Operation(summary = "Repartidores dados de baja", description = "Lista los repartidores dados de baja, para poder reactivarlos")
    public ResponseEntity<List<UserReportResponseDTO>> getInactiveRepartidores(Authentication authentication) {
        return ResponseEntity.ok(adminService.getInactiveRepartidoresReport(authentication.getName()));
    }

    @PatchMapping("/repartidores/{id}/deactivate")
    @Operation(summary = "Dar de baja repartidor", description = "Pasa sus clientes y pedidos abiertos a otro repartidor y lo da de baja")
    public ResponseEntity<ReassignResultDTO> deactivateRepartidor(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody(required = false) ReassignRepartidorRequestDTO request) {
        Long newId = request == null ? null : request.newRepartidorId();
        return ResponseEntity.ok(userService.deactivateRepartidor(authentication.getName(), id, newId));
    }

    @PatchMapping("/repartidores/{id}/reactivate")
    @Operation(summary = "Reactivar repartidor", description = "Vuelve a habilitar un repartidor dado de baja")
    public ResponseEntity<Void> reactivateRepartidor(Authentication authentication, @PathVariable Long id) {
        userService.reactivateRepartidor(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/clientes/{id}/repartidor")
    @Operation(summary = "Cambiar repartidor de un cliente", description = "Pasa un cliente (y sus pedidos abiertos) a otro repartidor activo")
    public ResponseEntity<ReassignResultDTO> moveClient(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody ReassignRepartidorRequestDTO request) {
        return ResponseEntity.ok(userService.moveClientToRepartidor(authentication.getName(), id, request.newRepartidorId()));
    }

    @PatchMapping("/users/{id}/password")
    @Operation(summary = "Cambiar contraseña", description = "Permite al admin cambiar la contraseña de un repartidor o cliente")
    public ResponseEntity<Void> resetUserPassword(@PathVariable Long id,
                                                  @Valid @RequestBody ResetPasswordRequestDTO request) {
        userService.resetPasswordByAdmin(id, request.newPassword());
        return ResponseEntity.noContent().build();
    }


}
