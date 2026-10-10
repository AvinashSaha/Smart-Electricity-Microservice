package com.sdem.citizen.exception;

public class CitizenNotFoundException extends RuntimeException {

    public CitizenNotFoundException(Long id) {
        super("Citizen " + id + " was not found");
    }
}
