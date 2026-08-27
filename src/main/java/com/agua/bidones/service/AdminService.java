package com.agua.bidones.service;

import com.agua.bidones.dto.UserReportResponseDTO;
import com.agua.bidones.dto.UserRequestDTO;
import com.agua.bidones.dto.UserResponseDTO;
import com.agua.bidones.enums.UserType;
import com.agua.bidones.model.User;
import com.agua.bidones.repository.IUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.directory.InvalidAttributeValueException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO registerAdmin(@Valid UserRequestDTO userRequest) {
        // Verificar si ya existe el usuario en la BD
        if (userRepository.existsByIdentifier(userRequest.getIdentifier())) {
            throw new RuntimeException("Ya existe un usuario registrado con ese identificador.");
        }
        User newUser = new User();
        newUser.setName(userRequest.getName());
        newUser.setUserType(UserType.ADMIN);
        newUser.setIdentifier(userRequest.getIdentifier());
        newUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        userRepository.save(newUser);

        return new UserResponseDTO(
                newUser.getUserType(),
                newUser.getName(),
                newUser.getIdentifier());
    }

    public @Nullable List<UserReportResponseDTO> getClientsReport(String adminIdentifier) {
        return userRepository.findClientesByAdminIdentifier(UserType.CLIENTE, adminIdentifier)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public @Nullable List<UserReportResponseDTO> getRepartidoresReport(String adminIdentifier) {
        return userRepository.findRepartidoresByAdminIdentifier(UserType.REPARTIDOR, adminIdentifier)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private UserReportResponseDTO toResponseDTO(User user) {
        return new UserReportResponseDTO(
                user.getName(),
                user.getIdentifier()
        );
    }
}
