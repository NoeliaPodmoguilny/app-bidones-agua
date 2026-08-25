package com.agua.bidones.service;

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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final IOrderRepository orderRepository;
    private final IUserRepository userRepository;
    private final NotificationService notificationService;


    public List<OrderResponseDTO> getOrdersByUserIdentifier(String identifier) {
        User cliente = userRepository.findUserByIdentifier(identifier);
        if(cliente==null){
            throw new RuntimeException("Usuario no encontrado");
        }
        return orderRepository.findByClienteOrderByCreatedAtDesc(cliente)
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
                order.getRepartidor().getName()
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
        User repartidor = getAuthenticatedRepartidor(repartidorIdentifier);
        return orderRepository.findByRepartidorOrderByCreatedAtDesc(repartidor)
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

    @Transactional
    public OrderResponseDTO deliverOrderByRepartidor(String repartidorIdentifier, Long orderId) {
        User repartidor = getAuthenticatedRepartidor(repartidorIdentifier);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        validateOrderBelongsToRepartidor(order, repartidor);
        if (order.getStatus() != Status.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden entregar pedidos pendientes");
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

        if (order.getStatus() != Status.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden rechazar pedidos pendientes");
        }

        order.setStatus(Status.RECHAZADO);
        order.setRejectionReason(request.rejectionReason());

        notificationService.createOrderRejectedNotification(order);

        return toResponseDTO(order);
    }

    public void deleteOrder(Long id) {
        Order pedido = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
        if(pedido.getStatus()!= Status.PENDIENTE){
            throw new RuntimeException("No se puede eliminar el pedido porque ya fue procesado");
        }
        orderRepository.delete(pedido);
    }


    public List<OrderResponseDTO> getOrdersReport(String authIdentifier, OrderFilterDTO orderFilterDTO){
        return orderRepository.findAll(OrderSpecification.withFilters(orderFilterDTO))
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

}

