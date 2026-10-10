package com.sdem.city;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Smart City microservice. Combines data from the Citizen and Electricity
 * Provider services into city wide reports (consumption, renewable share,
 * CO2 avoided, usage per district and per provider) and keeps a history of
 * report snapshots.
 */
@SpringBootApplication
public class SmartCityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCityApplication.class, args);
    }
}
