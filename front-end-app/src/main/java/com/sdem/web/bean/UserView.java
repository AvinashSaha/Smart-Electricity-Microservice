package com.sdem.web.bean;

/** A user as shown on the user management page (never includes the password). */
public record UserView(String username, String role) {
}
