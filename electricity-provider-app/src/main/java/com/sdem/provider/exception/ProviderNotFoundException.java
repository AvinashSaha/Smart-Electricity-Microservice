package com.sdem.provider.exception;

public class ProviderNotFoundException extends RuntimeException {

    public ProviderNotFoundException(Long id) {
        super("Provider " + id + " was not found");
    }
}
