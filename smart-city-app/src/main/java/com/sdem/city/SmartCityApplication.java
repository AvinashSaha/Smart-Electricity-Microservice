package com.sdem.city;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

/**
 * Smart City microservice.
 *
 * <p>One instance per city. On a schedule it requests monthly data from the
 * city's three electricity providers through the API gateway, collates it and
 * stores the aggregated totals permanently in its own database.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class SmartCityApplication {

    /** Name of this service's configuration file (smartcity.yml). */
    public static final String CONFIG_NAME = "smartcity";

    public static void main(String[] args) {
        create().run(args);
    }

    /** Builds the Spring application with this service's defaults. */
    public static SpringApplication create() {
        SpringApplication app = new SpringApplication(SmartCityApplication.class);
        app.setDefaultProperties(Map.of("spring.config.name", CONFIG_NAME));
        return app;
    }
}
