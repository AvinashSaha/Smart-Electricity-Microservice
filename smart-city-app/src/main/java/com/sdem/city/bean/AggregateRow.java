package com.sdem.city.bean;

import com.sdem.city.entities.AggregatedConsumption;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** One stored aggregate: a provider's totals for one month in this city. */
public record AggregateRow(
        String city,
        String provider,
        String month,
        BigDecimal totalKwh,
        long readingCount,
        long citizenCount,
        LocalDateTime collectedAt) {

    public static AggregateRow from(String city, AggregatedConsumption a) {
        return new AggregateRow(city, a.getProvider(), a.getMonth(), a.getTotalKwh(), a.getReadingCount(),
                a.getCitizenCount(), a.getCollectedAt());
    }
}
