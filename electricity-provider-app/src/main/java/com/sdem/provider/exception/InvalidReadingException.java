package com.sdem.provider.exception;

/** Raised when a reading breaks a business rule, for example it is lower than the previous one. */
public class InvalidReadingException extends RuntimeException {

    public InvalidReadingException(String message) {
        super(message);
    }
}
