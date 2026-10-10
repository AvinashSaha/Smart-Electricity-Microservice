package com.sdem.web.bean;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data for a line chart, serialised to JSON for Chart.js.
 *
 * @param title    chart title
 * @param unit     y axis unit
 * @param labels   x axis labels (months)
 * @param datasets one line per provider or city
 */
public record ChartData(String title, String unit, List<String> labels, List<Dataset> datasets) {

    /**
     * @param label  legend label
     * @param values one value per label; null where there is no data
     * @param dashed draw as a dashed line (used for the overall average)
     */
    public record Dataset(String label, List<BigDecimal> values, boolean dashed) {
    }
}
