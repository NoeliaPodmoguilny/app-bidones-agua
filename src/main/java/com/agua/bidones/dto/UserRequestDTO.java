package com.agua.bidones.dto;

import com.agua.bidones.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

    private UserType userType;
    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;
    @NotBlank(message = "El usuario no puede estar vacío")
    private String identifier;
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;

}
