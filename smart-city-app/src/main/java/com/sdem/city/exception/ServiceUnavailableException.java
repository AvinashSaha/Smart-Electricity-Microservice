package com.sdem.city.exception;

/** Raised when a downstream microservice cannot be reached. */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
