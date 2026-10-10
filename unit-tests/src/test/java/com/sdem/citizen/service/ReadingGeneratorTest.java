package com.sdem.citizen.service;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReadingGeneratorTest {

    private final Random random = new Random(42);

    @RepeatedTest(50)
    void valuesStayInsideTheRange() {
        BigDecimal value = ReadingGenerator.between(new BigDecimal("0.002"), new BigDecimal("0.020"), random);
        assertThat(value).isBetween(new BigDecimal("0.002"), new BigDecimal("0.020"));
    }

    @RepeatedTest(50)
    void nextReadingIsAlwaysHigher() {
        BigDecimal previous = new BigDecimal("1234.567");
        BigDecimal next = ReadingGenerator.next(previous, BigDecimal.ZERO, new BigDecimal("0.001"), random);
        assertThat(next).isGreaterThan(previous);
    }

    @Test
    void winterUsesMoreThanSummer() {
        assertThat(ReadingGenerator.seasonalFactor(Month.JANUARY)).isGreaterThan(1.2);
        assertThat(ReadingGenerator.seasonalFactor(Month.JULY)).isLessThan(0.8);
    }

    @Test
    void rejectsAnInvertedRange() {
        assertThatThrownBy(() -> ReadingGenerator.between(BigDecimal.TEN, BigDecimal.ONE, random))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
