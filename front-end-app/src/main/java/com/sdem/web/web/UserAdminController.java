package com.sdem.web.web;

import com.sdem.web.bean.NewUserForm;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** User management for administrators: list, create and delete users. */
@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    private final UserManagementService users;

    public UserAdminController(UserManagementService users) {
        this.users = users;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", users.list());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new NewUserForm());
        }
        return "admin/users";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") NewUserForm form, BindingResult result,
                         RedirectAttributes redirect, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", users.list());
            return "admin/users";
        }
        try {
            users.create(form.getUsername(), form.getPassword(), form.getRole());
            redirect.addFlashAttribute("message", "User '" + form.getUsername() + "' created.");
        } catch (UserManagementException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{username}/delete")
    public String delete(@PathVariable String username, @AuthenticationPrincipal UserDetails current,
                         RedirectAttributes redirect) {
        try {
            users.delete(username, current.getUsername());
            redirect.addFlashAttribute("message", "User '" + username + "' deleted.");
        } catch (UserManagementException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}
