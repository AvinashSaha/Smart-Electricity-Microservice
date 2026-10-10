package com.sdem.citizen.bean;

import com.sdem.citizen.entities.MeterType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Body sent to the provider's POST /api/v1/readings endpoint. */
public record ReadingPayload(String citizenId, MeterType meterType, BigDecimal readingKwh, LocalDateTime recordedAt) {
}
