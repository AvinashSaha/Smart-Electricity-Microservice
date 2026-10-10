package com.sdem.web.client;

/** A readable error raised when the gateway or a microservice rejects a call. */
public class GatewayException extends RuntimeException {

    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
