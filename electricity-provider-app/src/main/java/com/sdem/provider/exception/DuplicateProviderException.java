package com.sdem.provider.exception;

public class DuplicateProviderException extends RuntimeException {

    public DuplicateProviderException(String name) {
        super("A provider called '" + name + "' already exists");
    }
}
