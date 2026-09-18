package com.cropdeal.authservice.security;

import com.cropdeal.authservice.entity.AccountStatus;
import com.cropdeal.authservice.entity.UserAuth;
import com.cropdeal.authservice.repository.RevokedTokenRepository;
import com.cropdeal.authservice.repository.UserAuthRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final RevokedTokenRepository revokedTokenRepository;
    private final UserAuthRepository userAuthRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtils.validateToken(token)) {
                String tokenHash = JwtUtils.hashToken(token);

                // 1. Check if token is blacklisted in revoked_tokens
                if (revokedTokenRepository.existsByTokenHash(tokenHash)) {
                    log.warn("Access attempt with revoked JWT token");
                    filterChain.doFilter(request, response);
                    return;
                }

                try {
                    Long userId = jwtUtils.getUserIdFromToken(token);
                    Optional<UserAuth> userOpt = userAuthRepository.findById(userId);

                    if (userOpt.isPresent()) {
                        UserAuth user = userOpt.get();

                        if (user.getStatus() == AccountStatus.ACTIVE) {
                            // 2. Check if password was changed after token issue date
                            if (user.getPasswordChangedAt() != null) {
                                Date issuedAt = jwtUtils.getIssuedAtFromToken(token);
                                Date passwordChangedAt = Date.from(user.getPasswordChangedAt().atZone(ZoneId.systemDefault()).toInstant());

                                if (issuedAt != null && issuedAt.before(passwordChangedAt)) {
                                    log.warn("Access attempt with token issued prior to password change for user ID: {}", userId);
                                    filterChain.doFilter(request, response);
                                    return;
                                }
                            }

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            user,
                                            null,
                                            Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
                                    );
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                } catch (Exception e) {
                    log.error("Cannot set user authentication: {}", e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
