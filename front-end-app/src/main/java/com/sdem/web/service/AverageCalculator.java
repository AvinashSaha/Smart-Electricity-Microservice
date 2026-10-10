package com.sdem.web.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates averages on the fly. Averages are never stored anywhere.
 */
public final class AverageCalculator {

    private AverageCalculator() {
    }

    /**
     * Average consumption per citizen.
     *
     * @return total divided by citizens to 2 decimal places, or null when there are no citizens
     */
    public static BigDecimal perCitizen(BigDecimal totalKwh, long citizens) {
        if (citizens <= 0 || totalKwh == null) {
            return null;
        }
        return totalKwh.divide(BigDecimal.valueOf(citizens), 2, RoundingMode.HALF_UP);
    }
}
