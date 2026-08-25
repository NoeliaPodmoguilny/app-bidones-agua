package com.agua.bidones.controller;

import com.agua.bidones.dto.AuthLoginRequestDTO;
import com.agua.bidones.dto.AuthResponseDTO;
import com.agua.bidones.dto.PushTokenRequestDTO;
import com.agua.bidones.service.UserDetailsServiceImp;
import com.agua.bidones.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login con autenticación")
public class AuthController {

    private final UserDetailsServiceImp userDetailsService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthLoginRequestDTO userRequest) {
        AuthResponseDTO response = userDetailsService.loginUser(userRequest);
        return ResponseEntity.ok(response);
    }

}
