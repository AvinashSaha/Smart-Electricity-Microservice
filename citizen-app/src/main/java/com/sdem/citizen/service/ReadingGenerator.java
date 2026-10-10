package com.sdem.citizen.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.random.RandomGenerator;

/**
 * Generates meter values. Pure functions, so the random source can be fixed in tests.
 */
public final class ReadingGenerator {

    private ReadingGenerator() {
    }

    /** A random value between min and max (inclusive), rounded to 3 decimal places. */
    public static BigDecimal between(BigDecimal min, BigDecimal max, RandomGenerator random) {
        if (max.compareTo(min) < 0) {
            throw new IllegalArgumentException("max must not be less than min");
        }
        BigDecimal span = max.subtract(min);
        BigDecimal value = min.add(span.multiply(BigDecimal.valueOf(random.nextDouble())));
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * The next cumulative reading: the previous reading plus a random increase.
     * The increase is never less than 0.001 kWh, so readings always go up.
     */
    public static BigDecimal next(BigDecimal previous, BigDecimal minIncrease, BigDecimal maxIncrease,
                                  RandomGenerator random) {
        BigDecimal increase = between(minIncrease, maxIncrease, random).max(new BigDecimal("0.001"));
        return previous.add(increase);
    }

    /**
     * Seasonal multiplier: about 1.25 in January (more heating and lighting),
     * about 0.75 in July.
     */
    public static double seasonalFactor(Month month) {
        return 1.0 + 0.25 * Math.cos(2 * Math.PI * (month.getValue() - 1) / 12.0);
    }

    /** Applies a multiplier and keeps 3 decimal places. */
    public static BigDecimal scale(BigDecimal value, double factor) {
        return value.multiply(BigDecimal.valueOf(factor)).setScale(3, RoundingMode.HALF_UP);
    }
}
