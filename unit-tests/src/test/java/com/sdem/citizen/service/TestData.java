package com.sdem.citizen.service;

import com.sdem.citizen.config.SimulationProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Builds simulation settings for tests. */
final class TestData {

    private TestData() {
    }

    static SimulationProperties properties(int manual, int smart, int backfillMonths) {
        return new SimulationProperties("newcastle", List.of("a", "b", "c"), manual, smart,
                new BigDecimal("0.002"), new BigDecimal("0.020"), new BigDecimal("0.5"), new BigDecimal("5.0"),
                1.0, backfillMonths, new BigDecimal("180"), new BigDecimal("320"), 1.0,
                Map.of("a", 1.0, "b", 0.9, "c", 1.1));
    }
}
