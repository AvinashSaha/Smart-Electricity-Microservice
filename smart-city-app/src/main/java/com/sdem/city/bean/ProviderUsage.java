package com.sdem.city.bean;

import java.math.BigDecimal;

public record ProviderUsage(
        Long providerId,
        String providerName,
        int renewablePercent,
        long citizens,
        BigDecimal kwh,
        BigDecimal renewableKwh) {
}
