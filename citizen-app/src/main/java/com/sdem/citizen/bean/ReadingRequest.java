package com.sdem.citizen.bean;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A new cumulative meter reading. If readingAt is left out the current time is used.
 */
public record ReadingRequest(
        @NotNull @DecimalMin("0.00") BigDecimal readingKwh,
        @PastOrPresent LocalDateTime readingAt) {
}
