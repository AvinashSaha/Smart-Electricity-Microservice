package com.sdem.citizen.bean;

import com.sdem.citizen.entities.Citizen;
import com.sdem.citizen.entities.MeterType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CitizenResponse(String id, String provider, MeterType meterType, BigDecimal lastReadingKwh,
                              LocalDateTime lastReadingAt) {

    public static CitizenResponse from(Citizen c) {
        return new CitizenResponse(c.getId(), c.getProviderCode(), c.getMeterType(), c.getLastReadingKwh(),
                c.getLastReadingAt());
    }
}
