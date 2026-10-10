package com.sdem.web.web;

import com.sdem.web.config.FrontEndProperties;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Values every page needs: navigation lists and version numbers. */
@ControllerAdvice
public class GlobalModelAttributes {

    private final FrontEndProperties properties;

    public GlobalModelAttributes(FrontEndProperties properties) {
        this.properties = properties;
    }

    @ModelAttribute
    public void common(Model model) {
        model.addAttribute("navCities", properties.cities());
        model.addAttribute("navProviders", properties.providers());
        model.addAttribute("appVersion", properties.version());
        model.addAttribute("apiVersion", properties.apiVersion());
    }
}
