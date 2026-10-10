package com.sdem.web.web;

import com.sdem.web.bean.CityLoad;
import com.sdem.web.config.FrontEndProperties;
import com.sdem.web.exception.NotFoundException;
import com.sdem.web.service.CityDataService;
import com.sdem.web.service.DataViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Provider page: one provider's aggregated data across all cities. */
@Controller
public class ProviderController {

    private final FrontEndProperties properties;
    private final CityDataService data;
    private final DataViewService views;

    public ProviderController(FrontEndProperties properties, CityDataService data, DataViewService views) {
        this.properties = properties;
        this.data = data;
        this.views = views;
    }

    @GetMapping("/providers/{code}")
    public String provider(@PathVariable String code, @RequestParam(required = false) Integer months, Model model) {
        FrontEndProperties.Provider provider = properties.provider(code)
                .orElseThrow(() -> new NotFoundException("There is no provider '" + code + "'"));
        int range = data.months(months);
        List<CityLoad> loads = data.loadAll(range);
        model.addAttribute("provider", provider);
        model.addAttribute("months", range);
        model.addAttribute("unavailable", loads.stream().filter(l -> !l.available()).toList());
        model.addAttribute("table", views.providerTable(provider, loads, data.monthKeys(range)));
        return "provider";
    }
}
