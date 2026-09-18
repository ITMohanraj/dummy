package com.cropdeal.negotiationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NegotiationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NegotiationServiceApplication.class, args);
    }
}