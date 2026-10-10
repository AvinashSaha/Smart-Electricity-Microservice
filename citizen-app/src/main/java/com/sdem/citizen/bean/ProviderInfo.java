package com.sdem.citizen.bean;

import java.math.BigDecimal;

/** Provider details as returned by the Electricity Provider service. */
public record ProviderInfo(
        Long id,
        String name,
        BigDecimal unitRatePence,
        BigDecimal standingChargePence,
        int renewablePercent,
        boolean active) {
}
