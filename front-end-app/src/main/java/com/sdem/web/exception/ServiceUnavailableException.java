package com.sdem.web.exception;

/** Raised when a microservice cannot be reached or returns an error. */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
