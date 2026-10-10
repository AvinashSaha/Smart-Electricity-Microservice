package com.sdem.citizen.exception;

public class CitizenNotFoundException extends RuntimeException {

    public CitizenNotFoundException(String id) {
        super("Citizen " + id + " was not found");
    }
}
