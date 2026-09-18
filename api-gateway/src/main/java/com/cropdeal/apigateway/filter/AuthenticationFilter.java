package com.cropdeal.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Value("${cropdeal.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Correlation ID check / generation
            String correlationId = request.getHeaders().getFirst("X-Correlation-Id");
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            ServerHttpRequest.Builder mutatedRequestBuilder = request.mutate()
                    .header("X-Correlation-Id", correlationId);

            String path = request.getURI().getPath();

            // Check if endpoint requires authentication
            if (isSecured(path)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);
                try {
                    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                    Claims claims = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

                    mutatedRequestBuilder.header("X-User-Id", claims.getSubject())
                            .header("X-User-Email", claims.get("email", String.class))
                            .header("X-User-Role", claims.get("role", String.class));

                } catch (Exception e) {
                    return onError(exchange, "Unauthorized access: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                }
            }

            return chain.filter(exchange.mutate().request(mutatedRequestBuilder.build()).build());
        };
    }

    private boolean isSecured(String path) {
        // Authenticated Auth endpoints
        if (path.startsWith("/api/v1/auth/change-password") ||
            path.startsWith("/api/v1/auth/logout")) {
            return true;
        }

        // Public Auth endpoints
        if (path.startsWith("/api/v1/auth/")) {
            return false;
        }

        // Other public endpoints
        if (path.startsWith("/api/v1/crops/search") ||
            path.startsWith("/api/v1/crops/nearby") ||
            path.startsWith("/api/v1/prices/mandi") ||
            path.startsWith("/api/v1/prices/history") ||
            path.startsWith("/api/v1/prices/compare") ||
            path.startsWith("/api/v1/chatbot/ask") ||
            path.startsWith("/actuator") ||
            path.contains("/swagger-ui") ||
            path.contains("/v3/api-docs")) {
            return false;
        }
        return true;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {
    }
}
