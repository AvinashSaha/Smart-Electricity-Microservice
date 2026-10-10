package com.sdem.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Browser front end for SDEM. Server side rendered with Thymeleaf; every
 * call goes through the API gateway, never straight to a microservice.
 */
@SpringBootApplication
public class FrontEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrontEndApplication.class, args);
    }
}
