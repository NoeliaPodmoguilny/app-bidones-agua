package com.agua.bidones.dto;

import com.agua.bidones.enums.UserType;

public record UserResponseDTO(
        Long id,
        UserType userType,
        String name,
        String identifier
) {}
