package com.sdem.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single entry point for all communication. Every microservice instance that
 * registers with Eureka is exposed automatically as /{service-id}/** (for
 * example /provider-newcastle-a/api/v1/readings), so the citizen, smart city
 * and front end applications never need to know host names or ports.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
