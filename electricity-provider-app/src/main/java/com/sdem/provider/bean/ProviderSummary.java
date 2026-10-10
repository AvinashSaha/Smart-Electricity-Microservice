package com.sdem.provider.bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProviderSummary(
        String city,
        String provider,
        long readingCount,
        long citizenCount,
        BigDecimal totalKwh,
        LocalDateTime lastReadingAt) {
}
