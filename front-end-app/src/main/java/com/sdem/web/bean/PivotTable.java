package com.sdem.web.bean;

import java.math.BigDecimal;
import java.util.List;

/**
 * A table with one row per month and one column per provider (city table) or
 * per city (provider table).
 *
 * @param title   table heading
 * @param columns column headings, for example "Provider A"
 * @param rows    one row per month, oldest first
 * @param totals  column totals over all months
 */
public record PivotTable(String title, List<String> columns, List<Row> rows, Row totals) {

    /**
     * @param label            month label, for example "Mar 2026", or "Total"
     * @param values           kWh per column (null when no data)
     * @param totalKwh         sum of the row
     * @param citizens         citizens that sent readings
     * @param averagePerCitizen calculated average kWh per citizen
     */
    public record Row(String label, List<BigDecimal> values, BigDecimal totalKwh, long citizens,
                      BigDecimal averagePerCitizen) {
    }

    public boolean isEmpty() {
        return rows.stream().allMatch(r -> r.citizens() == 0);
    }
}
