package com.sdem.provider.bean;

import com.sdem.provider.entities.MeterType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A meter reading sent by the citizen microservice.
 *
 * @param citizenId  id of the citizen or smart meter
 * @param meterType  MANUAL or SMART
 * @param readingKwh cumulative value shown on the meter, must be higher than the previous one
 * @param recordedAt when the reading was taken; defaults to now when omitted
 */
public record ReadingRequest(
        @NotBlank String citizenId,
        @NotNull MeterType meterType,
        @NotNull @DecimalMin("0.0") BigDecimal readingKwh,
        @PastOrPresent LocalDateTime recordedAt) {
}
