package com.agua.bidones.service;

import com.agua.bidones.dto.ChangePasswordRequestDTO;
import com.agua.bidones.dto.UserReportResponseDTO;
import com.agua.bidones.dto.UserRequestDTO;
import com.agua.bidones.dto.UserResponseDTO;
import com.agua.bidones.enums.UserType;
import com.agua.bidones.model.User;
import com.agua.bidones.repository.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.agua.bidones.repository.IOrderRepository orderRepository;
    private final NotificationService notificationService;
    private final com.agua.bidones.repository.INotificationRepository notificationRepository;

    private static final List<com.agua.bidones.enums.Status> OPEN_STATUSES = List.of(
            com.agua.bidones.enums.Status.PENDIENTE, com.agua.bidones.enums.Status.EN_CAMINO);

    @Transactional
    public void updatePushToken(String identifier, String pushToken) {
        User user = userRepository.findUserByIdentifier(identifier);
        if (user == null) {
            throw new RuntimeException("No se encontró el usuario");
        }
        if (pushToken == null || pushToken.isBlank()) {
            user.setPushToken(null);
            return;
        }

        List<User> usuariosConElToken = userRepository.findByPushToken(pushToken);
        for (User u : usuariosConElToken) {
            if (!u.getIdentifier().equals(identifier)) {
                u.setPushToken(null);
                userRepository.save(u);
            }
        }
        user.setPushToken(pushToken);
    }

    public UserResponseDTO registerRepartidor(String adminIdentifier, @Valid UserRequestDTO userRequest) {
        User admin = userRepository.findUserByIdentifier(adminIdentifier);
        if(admin==null){
            throw new RuntimeException("Admin no encontrado");
        }
        if (admin.getUserType() != UserType.ADMIN) {
            throw new AccessDeniedException("Solo un admin puede crear repartidores");
        }
        if (userRepository.existsByIdentifier(userRequest.getIdentifier())) {
            throw new IllegalArgumentException("Ya existe un repartidor registrado con ese numero");
        }

        User newUser = new User();
        newUser.setName(userRequest.getName());
        newUser.setUserType(UserType.REPARTIDOR);
        newUser.setIdentifier(userRequest.getIdentifier());
        newUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        newUser.setAdmin(admin);

        userRepository.save(newUser);

        return new UserResponseDTO(
                newUser.getId(),
                newUser.getUserType(),
                newUser.getName(),
                newUser.getIdentifier());
    }

    @Transactional
    public UserResponseDTO registerClienteByRepartidor(String repartidorIdentifier, @Valid UserRequestDTO userRequest) {
        User repartidor = userRepository.findUserByIdentifier(repartidorIdentifier);
        if(repartidor==null){
            throw new RuntimeException("Repartidor no encontrado");
        }
        if (repartidor.getUserType() != UserType.REPARTIDOR) {
            throw new AccessDeniedException("Solo un repartidor puede crear clientes");
        }
        if (userRepository.existsByIdentifier(userRequest.getIdentifier())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese numero");
        }
        User newUser = new User();
        newUser.setName(userRequest.getName());
        newUser.setUserType(UserType.CLIENTE);
        newUser.setIdentifier(userRequest.getIdentifier());
        newUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        newUser.setRepartidor(repartidor);

        User savedUser = userRepository.save(newUser);
        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getUserType(),
                savedUser.getName(),
                savedUser.getIdentifier()
        );
    }

    @Transactional
    public void changePassword(String identifier, ChangePasswordRequestDTO request) {
        User user = userRepository.findUserByIdentifier(identifier);
        if(user==null){
            throw new RuntimeException("Usuario no encontrado");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }


    @Transactional
    public void deleteMyAccount(String identifier, String password) {
        User user = userRepository.findUserByIdentifier(identifier);
        if (user == null) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("La contraseña es incorrecta");
        }

        switch (user.getUserType()) {
            case CLIENTE -> {
                if (!orderRepository.findByClienteAndStatusIn(user, OPEN_STATUSES).isEmpty()) {
                    throw new IllegalStateException(
                            "Tenés pedidos en curso. Esperá a que se entreguen o eliminá los pendientes antes de borrar tu cuenta.");
                }
            }
            case REPARTIDOR -> {
                boolean hasClients = !userRepository
                        .findByRepartidorAndUserTypeOrderByNameAsc(user, UserType.CLIENTE).isEmpty();
                boolean hasOpenOrders = !orderRepository
                        .findByRepartidorAndStatusIn(user, OPEN_STATUSES).isEmpty();
                if (hasClients || hasOpenOrders) {
                    throw new IllegalStateException(
                            "Todavía tenés clientes o pedidos asignados. Pedile al administrador que los pase a otro repartidor.");
                }
            }
            case ADMIN -> {
                if (!userRepository.findRepartidoresByAdminIdentifier(UserType.REPARTIDOR, identifier).isEmpty()) {
                    throw new IllegalStateException(
                            "Primero tenés que dar de baja a tus repartidores para poder eliminar tu cuenta.");
                }
            }
        }

        notificationRepository.deleteByUser(user);
        user.setName("Cuenta eliminada");
        user.setIdentifier("eliminado-" + user.getId());
        user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        user.setPushToken(null);
        user.setActive(false);
    }

    @Transactional
    public void resetPasswordByAdmin(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        if (user.getUserType() == UserType.ADMIN) {
            throw new AccessDeniedException("No se puede restablecer la contraseña de un administrador desde esta sección");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
    }


    private User getAdminUser(String adminIdentifier) {
        User admin = userRepository.findUserByIdentifier(adminIdentifier);
        if (admin == null) {
            throw new EntityNotFoundException("Admin no encontrado");
        }
        if (admin.getUserType() != UserType.ADMIN) {
            throw new AccessDeniedException("Solo un admin puede hacer esto");
        }
        return admin;
    }

    private boolean belongsToAdmin(User repartidor, User admin) {
        return repartidor.getAdmin() != null && repartidor.getAdmin().getId().equals(admin.getId());
    }

    private User getOwnedRepartidor(User admin, Long repartidorId) {
        User repartidor = userRepository.findById(repartidorId)
                .orElseThrow(() -> new EntityNotFoundException("Repartidor no encontrado"));
        if (repartidor.getUserType() != UserType.REPARTIDOR || !belongsToAdmin(repartidor, admin)) {
            throw new AccessDeniedException("Este repartidor no pertenece a tu cuenta");
        }
        return repartidor;
    }

    private User getTargetRepartidor(User admin, Long newRepartidorId, Long excludeId) {
        if (newRepartidorId == null) {
            throw new IllegalArgumentException("Elegí a qué repartidor pasan los clientes");
        }
        if (newRepartidorId.equals(excludeId)) {
            throw new IllegalArgumentException("Elegí un repartidor distinto");
        }
        User target = getOwnedRepartidor(admin, newRepartidorId);
        if (!target.isAccountActive()) {
            throw new IllegalArgumentException("Ese repartidor está dado de baja. Elegí otro");
        }
        return target;
    }

    private int moveOpenOrders(List<com.agua.bidones.model.Order> orders, User target) {
        for (com.agua.bidones.model.Order order : orders) {
            order.setRepartidor(target);
            if (order.getStatus() == com.agua.bidones.enums.Status.PENDIENTE) {
                notificationService.createOrderPendingNotification(target, order);
            }
        }
        return orders.size();
    }

    @Transactional
    public com.agua.bidones.dto.ReassignResultDTO deactivateRepartidor(
            String adminIdentifier, Long repartidorId, Long newRepartidorId) {
        User admin = getAdminUser(adminIdentifier);
        User repartidor = getOwnedRepartidor(admin, repartidorId);
        if (!repartidor.isAccountActive()) {
            throw new IllegalStateException("Este repartidor ya está dado de baja");
        }

        List<User> clientes = userRepository
                .findByRepartidorAndUserTypeOrderByNameAsc(repartidor, UserType.CLIENTE);
        List<com.agua.bidones.model.Order> openOrders =
                orderRepository.findByRepartidorAndStatusIn(repartidor, OPEN_STATUSES);

        int ordersMoved = 0;
        if (!clientes.isEmpty() || !openOrders.isEmpty()) {
            User target = getTargetRepartidor(admin, newRepartidorId, repartidorId);
            clientes.forEach(cliente -> cliente.setRepartidor(target));
            ordersMoved = moveOpenOrders(openOrders, target);
        }

        repartidor.setActive(false);
        repartidor.setPushToken(null);
        return new com.agua.bidones.dto.ReassignResultDTO(clientes.size(), ordersMoved);
    }

    @Transactional
    public void reactivateRepartidor(String adminIdentifier, Long repartidorId) {
        User admin = getAdminUser(adminIdentifier);
        User repartidor = getOwnedRepartidor(admin, repartidorId);
        repartidor.setActive(true);
    }

    @Transactional
    public com.agua.bidones.dto.ReassignResultDTO moveClientToRepartidor(
            String adminIdentifier, Long clienteId, Long newRepartidorId) {
        User admin = getAdminUser(adminIdentifier);
        User cliente = userRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        if (cliente.getUserType() != UserType.CLIENTE
                || cliente.getRepartidor() == null
                || !belongsToAdmin(cliente.getRepartidor(), admin)) {
            throw new AccessDeniedException("Este cliente no pertenece a tu cuenta");
        }
        User target = getTargetRepartidor(admin, newRepartidorId, cliente.getRepartidor().getId());
        cliente.setRepartidor(target);
        int ordersMoved = moveOpenOrders(orderRepository.findByClienteAndStatusIn(cliente, OPEN_STATUSES), target);
        return new com.agua.bidones.dto.ReassignResultDTO(1, ordersMoved);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getMyRepartidor(String clienteIdentifier) {
        User cliente = userRepository.findUserByIdentifier(clienteIdentifier);
        if (cliente == null) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }
        if (cliente.getUserType() != UserType.CLIENTE) {
            throw new AccessDeniedException("Solo un cliente tiene repartidor asignado");
        }
        if (cliente.getRepartidor() == null) {
            throw new EntityNotFoundException("Todavía no tenés un repartidor asignado");
        }
        return toResponseDTO(cliente.getRepartidor());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getClientesOfRepartidor(String repartidorIdentifier) {
        User repartidor = userRepository.findUserByIdentifier(repartidorIdentifier);
        if (repartidor == null) {
            throw new RuntimeException("Repartidor no encontrado");
        }
        if (repartidor.getUserType() != UserType.REPARTIDOR) {
            throw new AccessDeniedException("Solo un repartidor puede realizar esta acción");
        }
        return userRepository
                .findByRepartidorAndUserTypeOrderByNameAsc(repartidor, UserType.CLIENTE)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public void resetClientPasswordByRepartidor(String repartidorIdentifier, Long clientId, String newPassword) {
        User repartidor = userRepository.findUserByIdentifier(repartidorIdentifier);
        if (repartidor == null) {
            throw new EntityNotFoundException("Repartidor no encontrado");
        }
        User cliente = userRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));

        if (cliente.getUserType() != UserType.CLIENTE || cliente.getRepartidor() == null
                || !cliente.getRepartidor().getId().equals(repartidor.getId())) {
            throw new AccessDeniedException("Este cliente no pertenece al repartidor");
        }
        cliente.setPassword(passwordEncoder.encode(newPassword));
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUserType(),
                user.getName(),
                user.getIdentifier()
        );
    }
}

