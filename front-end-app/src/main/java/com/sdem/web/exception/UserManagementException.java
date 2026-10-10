package com.sdem.web.exception;

/** Raised when a user management action is not allowed, for example deleting the last administrator. */
public class UserManagementException extends RuntimeException {

    public UserManagementException(String message) {
        super(message);
    }
}
