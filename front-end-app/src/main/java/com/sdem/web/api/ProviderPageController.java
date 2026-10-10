package com.sdem.web.api;

import com.sdem.web.bean.ProviderForm;
import com.sdem.web.client.GatewayClient;
import com.sdem.web.client.GatewayException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/providers")
public class ProviderPageController {

    private final GatewayClient gateway;

    public ProviderPageController(GatewayClient gateway) {
        this.gateway = gateway;
    }

    @GetMapping
    public String list(Model model) {
        try {
            model.addAttribute("providers", gateway.providers());
        } catch (GatewayException ex) {
            model.addAttribute("providers", List.of());
            model.addAttribute("error", ex.getMessage());
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ProviderForm());
        }
        return "providers";
    }

    @PostMapping
    public String create(@ModelAttribute("form") ProviderForm form, RedirectAttributes redirect) {
        try {
            gateway.createProvider(form);
            redirect.addFlashAttribute("message", "Provider " + form.getName() + " added.");
        } catch (GatewayException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            redirect.addFlashAttribute("form", form);
        }
        return "redirect:/providers";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            gateway.deactivateProvider(id);
            redirect.addFlashAttribute("message", "Provider deactivated.");
        } catch (GatewayException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/providers";
    }
}
