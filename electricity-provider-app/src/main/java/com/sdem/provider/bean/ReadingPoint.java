package com.sdem.provider.bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Minimal view of a reading used for aggregation. */
public record ReadingPoint(String citizenId, BigDecimal consumptionKwh, LocalDateTime recordedAt) {
}
