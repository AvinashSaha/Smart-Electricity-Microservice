package com.sdem.citizen.bean;

import java.math.BigDecimal;

/** Bill quote as returned by the Electricity Provider service. */
public record ProviderQuote(
        Long providerId,
        String providerName,
        BigDecimal kwh,
        int days,
        BigDecimal energyCost,
        BigDecimal standingCharge,
        BigDecimal total,
        BigDecimal renewableKwh) {
}
