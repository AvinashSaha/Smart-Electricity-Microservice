package com.sdem.city.bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CitySummary(
        LocalDateTime generatedAt,
        long citizenCount,
        BigDecimal totalKwh,
        BigDecimal averageKwhPerCitizen,
        BigDecimal renewableKwh,
        BigDecimal renewableSharePercent,
        BigDecimal co2AvoidedKg,
        List<ProviderUsage> byProvider,
        List<DistrictUsage> byDistrict) {
}
