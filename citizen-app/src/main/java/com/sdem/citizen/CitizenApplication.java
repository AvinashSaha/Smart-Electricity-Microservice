package com.sdem.citizen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Citizen microservice. Holds household (citizen) accounts and their smart
 * meter readings, works out consumption, and asks the Electricity Provider
 * service for a bill based on that consumption.
 */
@SpringBootApplication
public class CitizenApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitizenApplication.class, args);
    }
}
