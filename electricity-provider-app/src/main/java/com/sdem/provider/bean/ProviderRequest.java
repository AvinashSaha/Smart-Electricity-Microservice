package com.sdem.provider.bean;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Payload used to create or update a provider. */
public record ProviderRequest(
        @NotBlank String name,
        @NotNull @DecimalMin(value = "0.01") BigDecimal unitRatePence,
        @NotNull @DecimalMin(value = "0.00") BigDecimal standingChargePence,
        @Min(0) @Max(100) int renewablePercent) {
}
