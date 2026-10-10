package com.sdem.web.exception;

/** Raised for an unknown city or provider in the URL. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
