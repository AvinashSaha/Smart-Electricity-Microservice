package com.sdem.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.Map;

/**
 * North East Smart Electricity web application (group component).
 *
 * <p>A modular front end that consumes the Smart City microservice of every
 * city through the API gateway (RESTful JSON over HTTP). It provides user
 * authentication and management, data tables per city and per provider, and
 * interactive charts of monthly averages. Averages are calculated here, never
 * stored.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class FrontEndApplication {

    /** Name of this application's configuration file (frontend.yml). */
    public static final String CONFIG_NAME = "frontend";

    public static void main(String[] args) {
        create().run(args);
    }

    /** Builds the Spring application with this application's defaults. */
    public static SpringApplication create() {
        SpringApplication app = new SpringApplication(FrontEndApplication.class);
        app.setDefaultProperties(Map.of("spring.config.name", CONFIG_NAME));
        return app;
    }
}
