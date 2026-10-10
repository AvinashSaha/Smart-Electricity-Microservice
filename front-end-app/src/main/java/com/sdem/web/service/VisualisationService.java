package com.sdem.web.service;

import com.sdem.web.bean.AggregateRow;
import com.sdem.web.bean.ChartData;
import com.sdem.web.bean.CityLoad;
import com.sdem.web.config.FrontEndProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data visualisation module: builds the monthly average charts. All averages
 * are calculated here from the stored totals (kWh per citizen per month).
 */
@Service
public class VisualisationService {

    private final FrontEndProperties properties;

    public VisualisationService(FrontEndProperties properties) {
        this.properties = properties;
    }

    /** Monthly average of each provider in one city. */
    public ChartData cityChart(CityLoad load, List<String> months) {
        List<ChartData.Dataset> datasets = new ArrayList<>();
        for (FrontEndProperties.Provider p : properties.providers()) {
            List<BigDecimal> values = new ArrayList<>();
            for (String month : months) {
                AggregateRow r = CityDataService.find(load.rows(), p.code(), month);
                values.add(r == null ? null : AverageCalculator.perCitizen(r.totalKwh(), r.citizenCount()));
            }
            datasets.add(new ChartData.Dataset(p.name(), values, false));
        }
        return new ChartData("Monthly average per citizen in " + load.city().name(), "kWh per citizen",
                labels(months), datasets);
    }

    /** Monthly average of every city, plus the average of all cities together. */
    public ChartData regionChart(List<CityLoad> loads, List<String> months) {
        List<ChartData.Dataset> datasets = new ArrayList<>();
        BigDecimal[] regionTotals = new BigDecimal[months.size()];
        long[] regionCitizens = new long[months.size()];
        for (CityLoad load : loads) {
            List<BigDecimal> values = new ArrayList<>();
            for (int i = 0; i < months.size(); i++) {
                BigDecimal total = BigDecimal.ZERO;
                long citizens = 0;
                for (AggregateRow r : load.rows()) {
                    if (r.month().equals(months.get(i))) {
                        total = total.add(r.totalKwh());
                        citizens += r.citizenCount();
                    }
                }
                values.add(AverageCalculator.perCitizen(total, citizens));
                regionTotals[i] = regionTotals[i] == null ? total : regionTotals[i].add(total);
                regionCitizens[i] += citizens;
            }
            datasets.add(new ChartData.Dataset(load.city().name(), values, false));
        }
        List<BigDecimal> all = new ArrayList<>();
        for (int i = 0; i < months.size(); i++) {
            all.add(AverageCalculator.perCitizen(regionTotals[i], regionCitizens[i]));
        }
        datasets.add(new ChartData.Dataset("All cities", all, true));
        return new ChartData("Monthly average per citizen, all cities", "kWh per citizen", labels(months), datasets);
    }

    private static List<String> labels(List<String> months) {
        return months.stream().map(CityDataService::label).toList();
    }
}
