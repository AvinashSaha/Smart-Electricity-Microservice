package com.sdem.citizen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

/**
 * Citizen microservice.
 *
 * <p>One instance per city. It simulates, for each electricity provider, the
 * citizens who enter readings by hand (100 by default) and the smart meters
 * that send readings automatically (50 by default). Every reading is sent in
 * real time, through the API gateway, to the provider the citizen is
 * contracted with. Readings always increase.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class CitizenApplication {

    /** Name of this service's configuration file (citizen.yml). */
    public static final String CONFIG_NAME = "citizen";

    public static void main(String[] args) {
        create().run(args);
    }

    /** Builds the Spring application with this service's defaults. */
    public static SpringApplication create() {
        SpringApplication app = new SpringApplication(CitizenApplication.class);
        app.setDefaultProperties(Map.of("spring.config.name", CONFIG_NAME));
        return app;
    }
}
