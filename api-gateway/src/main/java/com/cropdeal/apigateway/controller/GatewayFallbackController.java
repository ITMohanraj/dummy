package com.cropdeal.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/gateway")
public class GatewayFallbackController {

    @GetMapping("/health-check")
    public Mono<ResponseEntity<Map<String, Object>>> healthCheck() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "api-gateway",
                "message", "CropDeal API Gateway is operating normally."
        )));
    }

    @GetMapping("/fallback")
    public Mono<ResponseEntity<Map<String, Object>>> fallback() {
        return Mono.just(ResponseEntity.status(503).body(Map.of(
                "status", "SERVICE_UNAVAILABLE",
                "error", "The requested downstream microservice is temporarily unavailable. Please try again later."
        )));
    }
}
