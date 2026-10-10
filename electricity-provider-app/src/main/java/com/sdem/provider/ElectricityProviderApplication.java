package com.sdem.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Electricity Provider microservice. Owns the list of energy suppliers,
 * their tariffs and renewable mix, and produces bill quotes for a given
 * amount of energy used.
 */
@SpringBootApplication
public class ElectricityProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectricityProviderApplication.class, args);
    }
}
