package com.sdem.citizen.exception;

/** Raised when a request clashes with existing data, for example a duplicate email. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
