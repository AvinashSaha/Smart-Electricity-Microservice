package com.sdem.city.bean;

import java.math.BigDecimal;
import java.util.List;

/**
 * Headline figures for the city, built from the stored aggregates.
 *
 * @param city      city name
 * @param totalKwh  total energy over the stored months
 * @param providers per provider totals and status
 */
public record CitySummary(String city, BigDecimal totalKwh, List<ProviderTotal> providers) {

    public record ProviderTotal(String provider, BigDecimal totalKwh, long readingCount, ProviderStatus status) {
    }
}
