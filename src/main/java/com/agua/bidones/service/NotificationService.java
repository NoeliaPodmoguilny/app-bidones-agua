package com.agua.bidones.service;

import com.agua.bidones.dto.NotificationResponseDTO;
import com.agua.bidones.enums.NotificationType;
import com.agua.bidones.model.Notification;
import com.agua.bidones.model.Order;
import com.agua.bidones.model.User;
import com.agua.bidones.repository.INotificationRepository;
import com.agua.bidones.repository.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final INotificationRepository notificationRepository;
    private final PushNotificationService pushNotificationService;
    private final IUserRepository userRepository;

    @Transactional
    public void createOrderPendingNotification(User repartidor, Order order) {
        User cliente = order.getCliente();

        Notification notification = new Notification();
        notification.setUser(repartidor);
        notification.setOrder(order);
        notification.setNotificationType(NotificationType.ORDEN_PENDIENTE);
        notification.setTitle("Nuevo pedido");
        notification.setMessage("Nuevo pedido de " + cliente.getName() + ".");
        notification.setRead(false);

        notificationRepository.save(notification);

        pushNotificationService.sendToUser(
                repartidor,
                "Nuevo pedido",
                "Nuevo pedido de " + cliente.getName() + "."
        );
    }

    @Transactional
    public void createOrderAcceptedNotification(Order order) {
        User cliente = order.getCliente();

        Notification notification = new Notification();
        notification.setUser(cliente);
        notification.setOrder(order);
        notification.setNotificationType(NotificationType.ORDEN_ENTREGADA);
        notification.setTitle("Pedido entregado");
        notification.setMessage("Tu pedido fue entregado correctamente.");
        notification.setRead(false);

        notificationRepository.save(notification);

        pushNotificationService.sendToUser(
                cliente,
                "Pedido entregado",
                "Tu pedido fue entregado correctamente."
        );
    }

    @Transactional
    public void createOrderOnTheWayNotification(Order order) {
        User cliente = order.getCliente();

        Notification notification = new Notification();
        notification.setUser(cliente);
        notification.setOrder(order);
        notification.setNotificationType(NotificationType.ORDEN_EN_CAMINO);
        notification.setTitle("Pedido en camino");
        notification.setMessage("Tu pedido fue aceptado y está en camino.");
        notification.setRead(false);

        notificationRepository.save(notification);

        pushNotificationService.sendToUser(
                cliente,
                "Pedido en camino",
                "Tu pedido fue aceptado y está en camino."
        );
    }

    @Transactional
    public void createOrderRejectedNotification(Order order) {
        User cliente = order.getCliente();

        Notification notification = new Notification();
        notification.setUser(cliente);
        notification.setOrder(order);
        notification.setNotificationType(NotificationType.ORDEN_RECHAZADA);
        notification.setTitle("Pedido rechazado");
        notification.setMessage("Tu pedido fue rechazado. Motivo: " + order.getRejectionReason());
        notification.setRead(false);

        notificationRepository.save(notification);
        pushNotificationService.sendToUser(
                cliente,
                "Pedido rechazado",
                "Tu pedido fue rechazado. Motivo: " + order.getRejectionReason()
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotificationsForUser(String identifier) {
        User user = getUserByIdentifier(identifier);

        return notificationRepository.findTop100ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(String identifier) {
        return notificationRepository.countByUserAndReadFalse(getUserByIdentifier(identifier));
    }

    @Transactional
    public void markAllAsRead(String identifier) {
        User user = getUserByIdentifier(identifier);

        List<Notification> unreadNotifications =
                notificationRepository.findByUserAndReadFalse(user);

        unreadNotifications.forEach(notification -> notification.setRead(true));
    }

    @Transactional
    public void markOneAsRead(String identifier, Long notificationId) {
        User user = getUserByIdentifier(identifier);

        Notification notification = notificationRepository
                .findByIdAndUser(notificationId, user)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada"));

        notification.setRead(true);
    }

    private User getUserByIdentifier(String identifier) {
        User user = userRepository.findUserByIdentifier(identifier);
        if(user==null){
            throw new RuntimeException("Usuario no encontrado");
        }
            return user;
    }

    private NotificationResponseDTO toResponseDTO(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getUser().getIdentifier(),
                notification.getNotificationType(),
                notification.getOrder() != null ? notification.getOrder().getId() : null,
                notification.getTitle(),
                notification.getMessage(),
                notification.getRead(),
                notification.getCreatedAt()
        );
    }
}
