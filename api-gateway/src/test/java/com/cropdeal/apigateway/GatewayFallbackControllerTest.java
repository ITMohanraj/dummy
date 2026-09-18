package com.cropdeal.apigateway;

import com.cropdeal.apigateway.controller.GatewayFallbackController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(controllers = GatewayFallbackController.class)
class GatewayFallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testHealthCheck() {
        webTestClient.get()
                .uri("/api/v1/gateway/health-check")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.service").isEqualTo("api-gateway");
    }

    @Test
    void testFallback() {
        webTestClient.get()
                .uri("/api/v1/gateway/fallback")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.status").isEqualTo("SERVICE_UNAVAILABLE");
    }
}
