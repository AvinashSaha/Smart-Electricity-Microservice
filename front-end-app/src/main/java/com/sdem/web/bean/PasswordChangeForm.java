package com.sdem.web.bean;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Form for a user changing their own password. */
public class PasswordChangeForm {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 64, message = "must be 8 to 64 characters")
    private String newPassword;

    @NotBlank
    private String confirmPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
