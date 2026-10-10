package com.sdem.web.api;

import com.sdem.web.bean.CitizenForm;
import com.sdem.web.bean.Provider;
import com.sdem.web.client.GatewayClient;
import com.sdem.web.client.GatewayException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/citizens")
public class CitizenPageController {

    private final GatewayClient gateway;

    public CitizenPageController(GatewayClient gateway) {
        this.gateway = gateway;
    }

    @GetMapping
    public String list(Model model) {
        try {
            List<Provider> providers = gateway.providers();
            model.addAttribute("citizens", gateway.citizens());
            model.addAttribute("providers", providers);
            model.addAttribute("providerNames", names(providers));
        } catch (GatewayException ex) {
            model.addAttribute("citizens", List.of());
            model.addAttribute("providers", List.of());
            model.addAttribute("providerNames", Map.of());
            model.addAttribute("error", ex.getMessage());
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CitizenForm());
        }
        return "citizens";
    }

    @PostMapping
    public String register(@ModelAttribute("form") CitizenForm form, RedirectAttributes redirect) {
        try {
            var citizen = gateway.registerCitizen(form);
            redirect.addFlashAttribute("message", citizen.fullName() + " registered.");
            return "redirect:/citizens/" + citizen.id();
        } catch (GatewayException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            redirect.addFlashAttribute("form", form);
            return "redirect:/citizens";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            List<Provider> providers = gateway.providers();
            model.addAttribute("citizen", gateway.citizen(id));
            model.addAttribute("readings", gateway.readings(id));
            model.addAttribute("consumption", gateway.consumption(id));
            model.addAttribute("providers", providers);
            model.addAttribute("providerNames", names(providers));
        } catch (GatewayException ex) {
            model.addAttribute("error", ex.getMessage());
            return "citizen";
        }
        try {
            model.addAttribute("bill", gateway.bill(id));
        } catch (GatewayException ex) {
            model.addAttribute("billError", ex.getMessage());
        }
        return "citizen";
    }

    @PostMapping("/{id}/readings")
    public String addReading(@PathVariable Long id, @RequestParam BigDecimal readingKwh,
                             RedirectAttributes redirect) {
        try {
            gateway.addReading(id, readingKwh);
            redirect.addFlashAttribute("message", "Reading of " + readingKwh + " kWh saved.");
        } catch (GatewayException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/citizens/" + id;
    }

    @PostMapping("/{id}/provider")
    public String switchProvider(@PathVariable Long id, @RequestParam Long providerId,
                                 RedirectAttributes redirect) {
        try {
            gateway.switchProvider(id, providerId);
            redirect.addFlashAttribute("message", "Provider switched.");
        } catch (GatewayException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/citizens/" + id;
    }

    private static Map<Long, String> names(List<Provider> providers) {
        return providers.stream().collect(Collectors.toMap(Provider::id, Provider::name, (a, b) -> a));
    }
}
