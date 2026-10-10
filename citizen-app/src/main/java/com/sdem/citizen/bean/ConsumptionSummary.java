package com.sdem.citizen.bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Energy used between the first and the latest meter reading.
 * When there are fewer than two readings, kwh and days are zero.
 */
public record ConsumptionSummary(
        Long citizenId,
        String postcode,
        Long providerId,
        int readingCount,
        LocalDateTime from,
        LocalDateTime to,
        int days,
        BigDecimal kwh) {
}
