package com.sdem.city.exception;

/** Raised when an electricity provider instance cannot be reached through the gateway. */
public class ProviderUnavailableException extends RuntimeException {

    public ProviderUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
