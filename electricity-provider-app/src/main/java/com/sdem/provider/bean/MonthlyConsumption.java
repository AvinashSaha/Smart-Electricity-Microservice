package com.sdem.provider.bean;

import java.math.BigDecimal;

/**
 * Totals for one calendar month. Averages are deliberately not included:
 * consumers calculate them from these totals.
 *
 * @param month        month in ISO form, for example "2026-03"
 * @param totalKwh     energy used by all citizens of this provider in the month
 * @param readingCount number of readings received
 * @param citizenCount number of different citizens that sent readings
 */
public record MonthlyConsumption(String month, BigDecimal totalKwh, long readingCount, long citizenCount) {
}
