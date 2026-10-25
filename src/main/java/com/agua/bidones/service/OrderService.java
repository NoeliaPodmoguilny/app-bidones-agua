package com.agua.bidones.service;

import com.agua.bidones.dto.AdminSummaryDTO;
import com.agua.bidones.dto.*;
import com.agua.bidones.enums.Status;
import com.agua.bidones.enums.UserType;
import com.agua.bidones.model.Order;
import com.agua.bidones.model.User;
import com.agua.bidones.repository.IOrderRepository;
import com.agua.bidones.repository.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final IOrderRepository orderRepository;
    private final IUserRepository userRepository;
    private final NotificationService notificationService;
    private final com.agua.bidones.repository.INotificationRepository notificationRepository;


    private static final List<Status> OPEN_STATUSES = List.of(Status.PENDIENTE, Status.EN_CAMINO);
    private static final int DEFAULT_DAYS = 30;

    private static Instant sinceDays(int days) {
        return LocalDate.now(OrderSpecification.ZONE).minusDays(days).atStartOfDay(OrderSpecification.ZONE).toInstant();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByUserIdentifier(String identifier) {
        return getOrdersByUserIdentifier(identifier, DEFAULT_DAYS);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByUserIdentifier(String identifier, int days) {
        User cliente = userRepository.findUserByIdentifier(identifier);
        if(cliente==null){
            throw new RuntimeException("Usuario no encontrado");
        }
        List<Order> orders = days > 0
                ? orderRepository.findRecentByCliente(cliente, sinceDays(days), OPEN_STATUSES)
                : orderRepository.findByClienteOrderByCreatedAtDesc(cliente);
        return orders
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }
    private OrderResponseDTO toResponseDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getStatus(),
                order.getQuantity(),
                order.getAddress(),
                order.getRejectionReason(),
                order.getCreatedAt(),
                order.getCliente().getName(),
                order.getRepartidor() != null ? order.getRepartidor().getName() : null
        );
    }

    @Transactional
    public OrderResponseDTO createOrder(String identifier, CreateOrderRequestDTO request) {
        User cliente = userRepository.findUserByIdentifier(identifier);
        if(cliente==null){
            throw new RuntimeException("Cliente no encontrado");
        }
        Order order = new Order();
        order.setCliente(cliente);
        order.setRepartidor(cliente.getRepartidor());
        order.setQuantity(request.quantity());
        order.setAddress(request.address());
        order.setStatus(Status.PENDIENTE);

        Order savedOrder = orderRepository.save(order);

        notificationService.createOrderPendingNotification(cliente.getRepartidor(), savedOrder);

        return toResponseDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<RepartidorOrderResponseDTO> getOrdersForRepartidor(String repartidorIdentifier) {
        return getOrdersForRepartidor(repartidorIdentifier, DEFAULT_DAYS);
    }

    @Transactional(readOnly = true)
    public List<RepartidorOrderResponseDTO> getOrdersForRepartidor(String repartidorIdentifier, int days) {
        User repartidor = getAuthenticatedRepartidor(repartidorIdentifier);
        List<Order> orders = days > 0
                ? orderRepository.findRecentByRepartidor(repartidor, sinceDays(days), OPEN_STATUSES)
                : orderRepository.findByRepartidorOrderByCreatedAtDesc(repartidor);
        return orders
                .stream()
                .map(this::toRepartidorOrderResponseDTO)
                .toList();
    }

    private RepartidorOrderResponseDTO toRepartidorOrderResponseDTO(Order order) {
        return new RepartidorOrderResponseDTO(
                order.getId(),
                order.getStatus(),
                order.getQuantity(),
                order.getAddress(),
                order.getRejectionReason(),
                order.getCreatedAt(),
                order.getCliente().getName(),
                order.getCliente().getIdentifier()
        );
    }

    // el pedido esta en camino
    @Transactional
    public OrderResponseDTO onTheWayOrderByRepartidor(String repartidorIdentifier, Long orderId) {
        User repartidor = getAuthenticatedRepartidor(repartidorIdentifier);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        validateOrderBelongsToRepartidor(order, repartidor);
        if (order.getStatus() != Status.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden enviar pedidos pendientes de entrega");
        }
        order.setStatus(Status.EN_CAMINO);
        order.setRejectionReason(null);
        notificationService.createOrderOnTheWayNotification(order);
        return toResponseDTO(order);
    }

    // pedido entregado
    @Transactional
    public OrderResponseDTO deliverOrderByRepartidor(String repartidorIdentifier, Long orderId) {
        User repartidor = getAuthenticatedRepartidor(repartidorIdentifier);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        validateOrderBelongsToRepartidor(order, repartidor);
        if (order.getStatus() != Status.EN_CAMINO) {
            throw new IllegalStateException("Solo se pueden entregar pedidos en camino");
        }
        order.setStatus(Status.ENTREGADO);
        order.setRejectionReason(null);
        notificationService.createOrderAcceptedNotification(order);
        return toResponseDTO(order);
    }

    private User getAuthenticatedRepartidor(String identifier) {
        User repartidor = userRepository.findUserByIdentifier(identifier);
        if(repartidor==null){
            throw new RuntimeException("Repartidor no encontrado");
        }
        if (repartidor.getUserType() != UserType.REPARTIDOR) {
            throw new AccessDeniedException("Solo un repartidor puede realizar esta accion");
        }
        return repartidor;
    }

    private void validateOrderBelongsToRepartidor(Order order, User repartidor) {
        if (order.getRepartidor() == null ||
                !order.getRepartidor().getId().equals(repartidor.getId())) {
            throw new AccessDeniedException("Este pedido no pertenece al repartidor autenticado");
        }
    }

    //pedido rechazado
    @Transactional
    public OrderResponseDTO rejectOrderByRepartidor(
            String repartidorIdentifier,
            Long orderId,
            RejectOrderRequestDTO request
    ) {
        User repartidor = getAuthenticatedRepartidor(repartidorIdentifier);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        validateOrderBelongsToRepartidor(order, repartidor);

        if (order.getStatus() != Status.PENDIENTE && order.getStatus() != Status.EN_CAMINO) {
            throw new IllegalStateException("Solo se pueden rechazar pedidos pendientes o en camino");
        }

        order.setStatus(Status.RECHAZADO);
        order.setRejectionReason(request.rejectionReason());

        notificationService.createOrderRejectedNotification(order);

        return toResponseDTO(order);
    }

    @Transactional
    public void deleteOrder(String identifier, Long id) {
        Order pedido = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
        if (pedido.getCliente() == null || !pedido.getCliente().getIdentifier().equals(identifier)) {
            throw new org.springframework.security.access.AccessDeniedException("Este pedido no es tuyo");
        }
        if(pedido.getStatus()!= Status.PENDIENTE){
            throw new IllegalStateException("No se puede eliminar el pedido porque ya fue procesado");
        }
        // la notificación "Nuevo pedido" del repartidor apunta al pedido: se borra primero
        notificationRepository.deleteByOrder(pedido);
        orderRepository.delete(pedido);
    }


    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersReport(String authIdentifier, OrderFilterDTO orderFilterDTO){
        if (orderFilterDTO.getDateFrom() == null && orderFilterDTO.getDateTo() == null
                && orderFilterDTO.getStatus() == null
                && (orderFilterDTO.getClienteName() == null || orderFilterDTO.getClienteName().isBlank())
                && (orderFilterDTO.getRepartidorName() == null || orderFilterDTO.getRepartidorName().isBlank())) {
            orderFilterDTO.setDateFrom(LocalDate.now(OrderSpecification.ZONE).minusDays(DEFAULT_DAYS));
        }
        return orderRepository.findAll(OrderSpecification.withFilters(authIdentifier, orderFilterDTO))
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public AdminSummaryDTO getAdminSummary(String adminIdentifier) {
        LocalDate today = LocalDate.now(OrderSpecification.ZONE);
        Instant start = today.atStartOfDay(OrderSpecification.ZONE).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(OrderSpecification.ZONE).toInstant();

        Map<String, Long> counts = new LinkedHashMap<>();
        for (Status status : Status.values()) {
            counts.put(status.name(), 0L);
        }
        for (Object[] row : orderRepository.countByStatusBetween(adminIdentifier, start, end)) {
            counts.put(((Status) row[0]).name(), ((Number) row[1]).longValue());
        }
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        long carryOver = orderRepository.countOpenBefore(adminIdentifier, start, OPEN_STATUSES);
        return new AdminSummaryDTO(counts, total, carryOver);
    }
}
