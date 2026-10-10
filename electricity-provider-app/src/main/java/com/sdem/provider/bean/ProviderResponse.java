package com.sdem.provider.bean;

import com.sdem.provider.entities.Provider;

import java.math.BigDecimal;

public record ProviderResponse(
        Long id,
        String name,
        BigDecimal unitRatePence,
        BigDecimal standingChargePence,
        int renewablePercent,
        boolean active) {

    public static ProviderResponse from(Provider p) {
        return new ProviderResponse(p.getId(), p.getName(), p.getUnitRatePence(),
                p.getStandingChargePence(), p.getRenewablePercent(), p.isActive());
    }
}
