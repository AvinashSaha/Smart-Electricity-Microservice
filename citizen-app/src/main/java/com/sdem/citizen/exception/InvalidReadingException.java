package com.sdem.citizen.exception;

/** Raised when a reading is rejected, for example because it is not higher than the last one. */
public class InvalidReadingException extends RuntimeException {

    public InvalidReadingException(String message) {
        super(message);
    }
}
