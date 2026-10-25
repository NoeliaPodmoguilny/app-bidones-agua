package com.agua.bidones.service;

import com.agua.bidones.dto.AuthLoginRequestDTO;
import com.agua.bidones.dto.AuthResponseDTO;
import com.agua.bidones.dto.UserResponseDTO;
import com.agua.bidones.repository.IUserRepository;
import com.agua.bidones.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImp implements UserDetailsService {

    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final IUserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        com.agua.bidones.model.User usuario = userRepo.findUserByIdentifier(identifier);
        if(usuario==null) {
            throw new UsernameNotFoundException("El usuario con teléfono " + identifier + " no fue encontrado");
        }
        return User.builder()
                .username(usuario.getIdentifier())
                .password(usuario.getPassword())
                .disabled(!usuario.isAccountActive())
                .build();
    }

    public AuthResponseDTO loginUser(AuthLoginRequestDTO authLoginRequest) {

        String identifier = authLoginRequest.identifier();
        String password = authLoginRequest.password();
        Authentication authentication = this.authenticate(identifier, password);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        com.agua.bidones.model.User usermodel = userRepo.findUserByIdentifier(identifier);
        if(usermodel==null){
            throw new UsernameNotFoundException("Usuario no encontrado");
        }
        String accessToken = jwtUtils.createToken(authentication, usermodel.getUserType().name());
        UserResponseDTO userDTO = new UserResponseDTO(
                usermodel.getId(),
                usermodel.getUserType(),
                usermodel.getName(),
                usermodel.getIdentifier()
        );
        AuthResponseDTO authResponseDTO =
                new AuthResponseDTO(userDTO, "login ok", accessToken, true);
        return authResponseDTO;
    }

    public Authentication authenticate(String identifier, String password) {
        UserDetails userDetails = this.loadUserByUsername(identifier);

        if (userDetails == null) {
            throw new BadCredentialsException("Contraseña y/o usuario incorrectos");
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Contraseña y/o usuario incorrectos");
        }
        if (!userDetails.isEnabled()) {
            throw new org.springframework.security.authentication.DisabledException(
                    "Tu cuenta fue dada de baja. Consultá con el administrador.");
        }
        return new UsernamePasswordAuthenticationToken(
                identifier,
                userDetails.getPassword(),
                userDetails.getAuthorities());
    }
}

