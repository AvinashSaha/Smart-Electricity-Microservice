package com.sdem.web.bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Monthly totals of one provider in one city, as returned by the Smart City API. */
public record AggregateRow(String city, String provider, String month, BigDecimal totalKwh, long readingCount,
                           long citizenCount, LocalDateTime collectedAt) {
}
