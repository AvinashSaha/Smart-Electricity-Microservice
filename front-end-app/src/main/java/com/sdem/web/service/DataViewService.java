package com.sdem.web.service;

import com.sdem.web.bean.AggregateRow;
import com.sdem.web.bean.CityLoad;
import com.sdem.web.bean.PivotTable;
import com.sdem.web.config.FrontEndProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data view module: builds the historical data tables.
 * <ul>
 *     <li>City table: one city, a column per provider.</li>
 *     <li>Provider table: one provider, a column per city.</li>
 * </ul>
 */
@Service
public class DataViewService {

    private final FrontEndProperties properties;

    public DataViewService(FrontEndProperties properties) {
        this.properties = properties;
    }

    /** Aggregated data of one city from its different providers. */
    public PivotTable cityTable(CityLoad load, List<String> months) {
        List<String> columns = properties.providers().stream().map(FrontEndProperties.Provider::name).toList();
        List<PivotTable.Row> rows = new ArrayList<>();
        for (String month : months) {
            List<AggregateRow> cells = new ArrayList<>();
            for (FrontEndProperties.Provider p : properties.providers()) {
                cells.add(CityDataService.find(load.rows(), p.code(), month));
            }
            rows.add(row(CityDataService.label(month), cells));
        }
        return new PivotTable(load.city().name() + " by provider", columns, rows, totals(rows, columns.size()));
    }

    /** Aggregated data of one provider from the different cities. */
    public PivotTable providerTable(FrontEndProperties.Provider provider, List<CityLoad> loads, List<String> months) {
        List<String> columns = loads.stream().map(l -> l.city().name()).toList();
        List<PivotTable.Row> rows = new ArrayList<>();
        for (String month : months) {
            List<AggregateRow> cells = new ArrayList<>();
            for (CityLoad load : loads) {
                cells.add(CityDataService.find(load.rows(), provider.code(), month));
            }
            rows.add(row(CityDataService.label(month), cells));
        }
        return new PivotTable(provider.name() + " by city", columns, rows, totals(rows, columns.size()));
    }

    private static PivotTable.Row row(String label, List<AggregateRow> cells) {
        List<BigDecimal> values = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        long citizens = 0;
        for (AggregateRow cell : cells) {
            if (cell == null) {
                values.add(null);
            } else {
                values.add(cell.totalKwh());
                total = total.add(cell.totalKwh());
                citizens += cell.citizenCount();
            }
        }
        return new PivotTable.Row(label, values, total, citizens, AverageCalculator.perCitizen(total, citizens));
    }

    /** Column totals. The average is the mean monthly kWh per citizen over the period. */
    private static PivotTable.Row totals(List<PivotTable.Row> rows, int columnCount) {
        List<BigDecimal> sums = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            BigDecimal sum = null;
            for (PivotTable.Row r : rows) {
                BigDecimal v = r.values().get(i);
                if (v != null) {
                    sum = sum == null ? v : sum.add(v);
                }
            }
            sums.add(sum);
        }
        BigDecimal total = rows.stream().map(PivotTable.Row::totalKwh).reduce(BigDecimal.ZERO, BigDecimal::add);
        long citizenMonths = rows.stream().mapToLong(PivotTable.Row::citizens).sum();
        return new PivotTable.Row("Total", sums, total, citizenMonths,
                AverageCalculator.perCitizen(total, citizenMonths));
    }
}
