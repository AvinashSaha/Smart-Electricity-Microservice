package com.sdem.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    /** Injected so tests can control "now". */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
