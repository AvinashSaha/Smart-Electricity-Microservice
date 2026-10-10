package com.sdem.web.web;

import com.sdem.web.bean.CityLoad;
import com.sdem.web.config.FrontEndProperties;
import com.sdem.web.exception.NotFoundException;
import com.sdem.web.service.CityDataService;
import com.sdem.web.service.DataViewService;
import com.sdem.web.service.VisualisationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * City module page. The same component serves every city: a data table of the
 * city's providers and a chart of each provider's monthly average.
 */
@Controller
public class CityController {

    private final FrontEndProperties properties;
    private final CityDataService data;
    private final DataViewService views;
    private final VisualisationService charts;

    public CityController(FrontEndProperties properties, CityDataService data, DataViewService views,
                          VisualisationService charts) {
        this.properties = properties;
        this.data = data;
        this.views = views;
        this.charts = charts;
    }

    @GetMapping("/cities/{id}")
    public String city(@PathVariable String id, @RequestParam(required = false) Integer months, Model model) {
        FrontEndProperties.City city = properties.city(id)
                .orElseThrow(() -> new NotFoundException("There is no city called '" + id + "'"));
        int range = data.months(months);
        List<String> keys = data.monthKeys(range);
        CityLoad load = data.load(city, range);
        model.addAttribute("city", city);
        model.addAttribute("months", range);
        model.addAttribute("load", load);
        model.addAttribute("table", views.cityTable(load, keys));
        model.addAttribute("chart", charts.cityChart(load, keys));
        return "city";
    }
}
