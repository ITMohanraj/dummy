package com.cropdeal.negotiationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway"),
                        new Server().url("http://localhost:8095").description("Direct Negotiation Service")
                ))
                .info(new Info()
                        .title("CropDeal Negotiation Service API")
                        .description("Multi-Dealer Independent Price Negotiation and Counter-Offer APIs")
                        .version("1.0.0")
                        .contact(new Contact().name("CropDeal Team").email("support@cropdeal.com")));
    }
}