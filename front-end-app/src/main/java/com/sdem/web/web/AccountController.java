package com.sdem.web.web;

import com.sdem.web.bean.PasswordChangeForm;
import com.sdem.web.exception.UserManagementException;
import com.sdem.web.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Lets any signed in user change their own password. */
@Controller
public class AccountController {

    private final UserManagementService users;

    public AccountController(UserManagementService users) {
        this.users = users;
    }

    @GetMapping("/account/password")
    public String form(Model model) {
        model.addAttribute("form", new PasswordChangeForm());
        return "account/password";
    }

    @PostMapping("/account/password")
    public String change(@AuthenticationPrincipal UserDetails user,
                         @Valid @ModelAttribute("form") PasswordChangeForm form, BindingResult result,
                         RedirectAttributes redirect, Model model) {
        if (!result.hasFieldErrors("newPassword") && form.getNewPassword() != null
                && !form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "does not match the new password");
        }
        if (result.hasErrors()) {
            return "account/password";
        }
        try {
            users.changePassword(user.getUsername(), form.getCurrentPassword(), form.getNewPassword());
        } catch (UserManagementException ex) {
            model.addAttribute("error", ex.getMessage());
            return "account/password";
        }
        redirect.addFlashAttribute("message", "Your password has been changed.");
        return "redirect:/";
    }
}
