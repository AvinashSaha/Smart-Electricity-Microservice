package com.sdem.web.web;

import com.sdem.web.bean.CityLoad;
import com.sdem.web.bean.PivotTable;
import com.sdem.web.service.CityDataService;
import com.sdem.web.service.DataViewService;
import com.sdem.web.service.VisualisationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** North East region overview: all cities chart and a summary card per city. */
@Controller
public class DashboardController {

    private final CityDataService data;
    private final DataViewService views;
    private final VisualisationService charts;

    public DashboardController(CityDataService data, DataViewService views, VisualisationService charts) {
        this.data = data;
        this.views = views;
        this.charts = charts;
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) Integer months, Model model) {
        int range = data.months(months);
        List<String> keys = data.monthKeys(range);
        List<CityLoad> loads = data.loadAll(range);

        List<CityCard> cards = new ArrayList<>();
        BigDecimal regionTotal = BigDecimal.ZERO;
        long citizenMonths = 0;
        for (CityLoad load : loads) {
            PivotTable table = views.cityTable(load, keys);
            cards.add(new CityCard(load, table.totals()));
            regionTotal = regionTotal.add(table.totals().totalKwh());
            citizenMonths += table.totals().citizens();
        }
        model.addAttribute("months", range);
        model.addAttribute("cards", cards);
        model.addAttribute("regionTotal", regionTotal);
        model.addAttribute("regionAverage", com.sdem.web.service.AverageCalculator.perCitizen(regionTotal, citizenMonths));
        model.addAttribute("citiesOnline", loads.stream().filter(CityLoad::available).count());
        model.addAttribute("chart", charts.regionChart(loads, keys));
        return "dashboard";
    }

    /** Summary of one city for the dashboard. */
    public record CityCard(CityLoad load, PivotTable.Row totals) {
    }
}
