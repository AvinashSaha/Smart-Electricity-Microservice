package com.sdem.citizen.bean;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** A reading typed in by a citizen. */
public record ManualReadingRequest(@NotNull @DecimalMin("0.0") BigDecimal readingKwh) {
}
