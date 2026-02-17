package com.operator.infrastructure.security;

import com.operator.common.enums.UserRole;
import com.operator.common.enums.UserStatus;
import com.operator.core.security.domain.User;
import com.operator.core.security.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * JWT Authentication Filter
 *
 * Validates JWT tokens and sets authentication in Spring Security context
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                      HttpServletResponse response,
                                      FilterChain filterChain) throws ServletException, IOException {
        // Get JWT token from Authorization header
        String token = getTokenFromRequest(request);

        // Validate token and set authentication
        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                Optional<User> userOptional = userRepository.findById(userId);

                if (userOptional.isPresent() && userOptional.get().getStatus() == UserStatus.ACTIVE) {
                    User user = userOptional.get();
                    UserPrincipal userPrincipal = UserPrincipal.create(user);

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userPrincipal,
                            null,
                            getAuthorities(userPrincipal)
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("User authenticated successfully: {}", user.getUsername());
                } else {
                    log.warn("User not found or inactive: {}", userId);
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "User not found or inactive");
                    return;
                }
            } catch (Exception e) {
                log.error("Error authenticating user", e);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Convert role to granted authority
     */
    private Collection<org.springframework.security.core.GrantedAuthority> getAuthorities(UserPrincipal userPrincipal) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userPrincipal.getRole().name()));
    }

    /**
     * Send error response
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":false,\"error\":\"" + message + "\"}");
    }
}
