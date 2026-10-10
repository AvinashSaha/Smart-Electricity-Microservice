package com.sdem.provider.service;

import com.sdem.provider.bean.MonthlyConsumption;
import com.sdem.provider.bean.ReadingPoint;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonthlyAggregatorTest {

    @Test
    void groupsReadingsByMonthAndCountsCitizens() {
        List<ReadingPoint> points = List.of(
                new ReadingPoint("C1", new BigDecimal("10.5"), LocalDateTime.of(2026, 3, 2, 10, 0)),
                new ReadingPoint("C1", new BigDecimal("4.5"), LocalDateTime.of(2026, 3, 20, 10, 0)),
                new ReadingPoint("C2", new BigDecimal("7.0"), LocalDateTime.of(2026, 3, 31, 23, 59)),
                new ReadingPoint("C2", new BigDecimal("3.0"), LocalDateTime.of(2026, 4, 1, 0, 0)));

        List<MonthlyConsumption> result = MonthlyAggregator.aggregate(points,
                List.of(YearMonth.of(2026, 3), YearMonth.of(2026, 4)));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).month()).isEqualTo("2026-03");
        assertThat(result.get(0).totalKwh()).isEqualByComparingTo("22.0");
        assertThat(result.get(0).readingCount()).isEqualTo(3);
        assertThat(result.get(0).citizenCount()).isEqualTo(2);
        assertThat(result.get(1).totalKwh()).isEqualByComparingTo("3.0");
        assertThat(result.get(1).citizenCount()).isEqualTo(1);
    }

    @Test
    void reportsEmptyMonthsAsZeroAndIgnoresOtherMonths() {
        List<ReadingPoint> points = List.of(
                new ReadingPoint("C1", new BigDecimal("5"), LocalDateTime.of(2025, 12, 5, 9, 0)));

        List<MonthlyConsumption> result = MonthlyAggregator.aggregate(points, List.of(YearMonth.of(2026, 1)));

        assertThat(result).singleElement().satisfies(m -> {
            assertThat(m.totalKwh()).isEqualByComparingTo("0");
            assertThat(m.readingCount()).isZero();
        });
    }

    @Test
    void lastMonthsIsOldestFirstAndIncludesCurrentMonth() {
        assertThat(MonthlyAggregator.lastMonths(YearMonth.of(2026, 2), 3))
                .containsExactly(YearMonth.of(2025, 12), YearMonth.of(2026, 1), YearMonth.of(2026, 2));
    }

    @Test
    void rejectsSillyMonthCounts() {
        assertThatThrownBy(() -> MonthlyAggregator.lastMonths(YearMonth.now(), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MonthlyAggregator.lastMonths(YearMonth.now(), 61))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
