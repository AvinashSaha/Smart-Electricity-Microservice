package com.sdem.provider.bean;

import java.math.BigDecimal;

/**
 * Cost breakdown for a given consumption over a number of days.
 * All money values are in pounds sterling, rounded to 2 decimal places.
 */
public record BillQuote(
        Long providerId,
        String providerName,
        BigDecimal kwh,
        int days,
        BigDecimal energyCost,
        BigDecimal standingCharge,
        BigDecimal total,
        BigDecimal renewableKwh) {
}
