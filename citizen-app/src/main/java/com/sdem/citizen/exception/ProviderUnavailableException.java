package com.sdem.citizen.exception;

/** Raised when the provider cannot be reached through the gateway. */
public class ProviderUnavailableException extends RuntimeException {

    public ProviderUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
