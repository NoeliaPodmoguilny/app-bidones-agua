package com.agua.bidones.controller;

import com.agua.bidones.dto.AuthLoginRequestDTO;
import com.agua.bidones.dto.AuthResponseDTO;
import com.agua.bidones.dto.PushTokenRequestDTO;
import com.agua.bidones.security.LoginAttemptService;
import com.agua.bidones.service.UserDetailsServiceImp;
import com.agua.bidones.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final LoginAttemptService loginAttempts;

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthLoginRequestDTO userRequest,
                                                 HttpServletRequest request) {
        String ip = LoginAttemptService.clientIp(request);
        String identifier = userRequest.identifier();

        loginAttempts.checkAllowed(ip, identifier);
        try {
            AuthResponseDTO response = userDetailsService.loginUser(userRequest);
            loginAttempts.recordSuccess(ip, identifier);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            loginAttempts.recordFailure(ip, identifier);
            throw e;
        }
    }

}
