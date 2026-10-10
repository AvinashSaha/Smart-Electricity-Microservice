package com.sdem.web.bean;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Form for an administrator creating a user. */
public class NewUserForm {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9._-]{3,30}$", message = "must be 3 to 30 letters, digits, dots, dashes or underscores")
    private String username;

    @NotBlank
    @Size(min = 8, max = 64, message = "must be 8 to 64 characters")
    private String password;

    @NotBlank
    @Pattern(regexp = "^(ADMIN|USER)$", message = "must be ADMIN or USER")
    private String role = "USER";

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
