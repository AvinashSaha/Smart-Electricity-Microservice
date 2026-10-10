package com.sdem.citizen.bean;

import com.sdem.citizen.entities.MeterReading;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReadingResponse(Long id, BigDecimal readingKwh, LocalDateTime readingAt) {

    public static ReadingResponse from(MeterReading r) {
        return new ReadingResponse(r.getId(), r.getReadingKwh(), r.getReadingAt());
    }
}
