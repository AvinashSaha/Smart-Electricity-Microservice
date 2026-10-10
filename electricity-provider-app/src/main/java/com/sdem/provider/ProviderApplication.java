package com.sdem.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.Map;

/**
 * Electricity Provider microservice.
 *
 * <p>One running instance represents one provider (A, B or C) in one city. It
 * receives meter readings from citizens and smart meters in real time and
 * stores them permanently in its own H2 database file under {@code data/}.</p>
 *
 * <p>Example: {@code java -jar electricity-provider-app-exec.jar
 * --sdem.provider.city=newcastle --sdem.provider.code=a --server.port=8111}</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ProviderApplication {

    /** Name of this service's configuration file (provider.yml), unique so several services can share a JVM. */
    public static final String CONFIG_NAME = "provider";

    public static void main(String[] args) {
        create().run(args);
    }

    /**
     * Builds the Spring application with this service's defaults. Used by
     * {@link #main(String[])} and by the main program that runs many instances.
     */
    public static SpringApplication create() {
        SpringApplication app = new SpringApplication(ProviderApplication.class);
        app.setDefaultProperties(Map.of("spring.config.name", CONFIG_NAME));
        return app;
    }
}
