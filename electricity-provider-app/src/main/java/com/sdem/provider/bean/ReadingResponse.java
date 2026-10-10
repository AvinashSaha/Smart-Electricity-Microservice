package com.sdem.provider.bean;

import com.sdem.provider.entities.MeterReading;
import com.sdem.provider.entities.MeterType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReadingResponse(
        Long id,
        String citizenId,
        MeterType meterType,
        BigDecimal readingKwh,
        BigDecimal consumptionKwh,
        LocalDateTime recordedAt) {

    public static ReadingResponse from(MeterReading r) {
        return new ReadingResponse(r.getId(), r.getCitizenId(), r.getMeterType(), r.getReadingKwh(),
                r.getConsumptionKwh(), r.getRecordedAt());
    }
}
