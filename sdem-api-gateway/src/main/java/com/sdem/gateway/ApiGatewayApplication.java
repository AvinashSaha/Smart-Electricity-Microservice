package com.sdem.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single entry point for every client. Requests under /api/** are routed to
 * the matching microservice, resolved through the Eureka registry and load
 * balanced with the "lb://" scheme. Routes are declared in application.yml.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
