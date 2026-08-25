package com.agua.bidones.service;

import com.agua.bidones.dto.ChangePasswordRequestDTO;
import com.agua.bidones.dto.UserRequestDTO;
import com.agua.bidones.dto.UserResponseDTO;
import com.agua.bidones.enums.UserType;
import com.agua.bidones.model.User;
import com.agua.bidones.repository.IUserRepository;
import jakarta.validation.Valid;
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

    // Añadir pushToken para notificaciones
    @Transactional
    public void updatePushToken(String identifier, String pushToken) {
        User user = userRepository.findUserByIdentifier(identifier);
        if (user == null) {
            throw new RuntimeException("No se encontró el usuario");
        }
        // Si viene vacío o nulo (LOGOUT)
        if (pushToken == null || pushToken.isBlank()) {
            user.setPushToken(null);
            return; // Terminamos acá
        }

        // Si viene un token válido, aseguramos que ningún otro usuario lo tenga guardado
        List<User> usuariosConElToken = userRepository.findByPushToken(pushToken);
        for (User u : usuariosConElToken) {
            if (!u.getIdentifier().equals(identifier)) {
                u.setPushToken(null);
                userRepository.save(u);
            }
        }
        // asignamos al usuario actual
        user.setPushToken(pushToken);
    }

    //Registrar nuevos repartidores
    public UserResponseDTO registerRepartidor(String adminIdentifier, @Valid UserRequestDTO userRequest) {
        //busco el admin al que pertenece
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
                newUser.getUserType(),
                newUser.getName(),
                newUser.getIdentifier());
    }

    //Registrar clientes por repartidor
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
            throw new BadCredentialsException("La contraseña actual es incorrecta");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }
}

