package com.sdem.city.bean;

import java.math.BigDecimal;

/** Monthly totals as returned by an Electricity Provider instance. */
public record ProviderMonthly(String month, BigDecimal totalKwh, long readingCount, long citizenCount) {
}
