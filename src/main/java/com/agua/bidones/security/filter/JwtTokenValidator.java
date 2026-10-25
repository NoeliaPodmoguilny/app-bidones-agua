package com.agua.bidones.security.filter;

import com.agua.bidones.repository.IUserRepository;
import com.agua.bidones.utils.JwtUtils;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


public class JwtTokenValidator extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final IUserRepository userRepo;

    public JwtTokenValidator(JwtUtils jwtUtils, IUserRepository userRepo) {
        this.jwtUtils = jwtUtils;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            Authentication authentication = null;
            try {
                DecodedJWT decodedJWT = jwtUtils.validateToken(authHeader.substring(7));
                String username = jwtUtils.extractUsername(decodedJWT);
                String role = resolveRole(decodedJWT, username);
                if (role != null) {
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                    authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                }
            } catch (JWTVerificationException | NullPointerException ex) {
                authentication = null;
            }
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveRole(DecodedJWT decodedJWT, String username) {
        Claim claim = decodedJWT.getClaim("role");
        String role = (claim != null && !claim.isNull() && !claim.isMissing()) ? claim.asString() : null;
        if (role != null && !"REPARTIDOR".equals(role)) {
            return role;
        }
        com.agua.bidones.model.User user = userRepo.findUserByIdentifier(username);
        if (user == null || user.getUserType() == null || !user.isAccountActive()) {
            return null;
        }
        return role != null ? role : user.getUserType().name();
    }
}
