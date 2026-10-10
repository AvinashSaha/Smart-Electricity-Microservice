package com.sdem.web.api;

import com.sdem.web.client.GatewayClient;
import com.sdem.web.client.GatewayException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardController {

    private final GatewayClient gateway;

    public DashboardController(GatewayClient gateway) {
        this.gateway = gateway;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        try {
            model.addAttribute("summary", gateway.citySummary());
        } catch (GatewayException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "index";
    }

    @PostMapping("/snapshots")
    public String snapshot(RedirectAttributes redirect) {
        try {
            gateway.takeSnapshot();
            redirect.addFlashAttribute("message", "Snapshot saved.");
        } catch (GatewayException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/";
    }
}
