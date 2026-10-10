package com.sdem.provider.service;

import com.sdem.provider.bean.MonthlyConsumption;
import com.sdem.provider.bean.ReadingPoint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Groups readings into calendar months. A pure function with no Spring or
 * database dependencies, so it is simple to unit test.
 */
public final class MonthlyAggregator {

    private MonthlyAggregator() {
    }

    /**
     * @param points readings to group
     * @param months the months to report, oldest first; months with no readings are reported as zero
     * @return one entry per requested month, oldest first
     */
    public static List<MonthlyConsumption> aggregate(List<ReadingPoint> points, List<YearMonth> months) {
        Map<YearMonth, Bucket> buckets = new TreeMap<>();
        months.forEach(m -> buckets.put(m, new Bucket()));
        for (ReadingPoint p : points) {
            Bucket bucket = buckets.get(YearMonth.from(p.recordedAt()));
            if (bucket != null) {
                bucket.add(p);
            }
        }
        List<MonthlyConsumption> result = new ArrayList<>();
        buckets.forEach((month, b) -> result.add(new MonthlyConsumption(month.toString(),
                b.total.setScale(3, RoundingMode.HALF_UP), b.readings, b.citizens.size())));
        return result;
    }

    /** The last {@code count} months up to and including {@code current}, oldest first. */
    public static List<YearMonth> lastMonths(YearMonth current, int count) {
        if (count < 1 || count > 60) {
            throw new IllegalArgumentException("months must be between 1 and 60");
        }
        List<YearMonth> months = new ArrayList<>();
        for (int i = count - 1; i >= 0; i--) {
            months.add(current.minusMonths(i));
        }
        return months;
    }

    private static final class Bucket {
        private BigDecimal total = BigDecimal.ZERO;
        private long readings;
        private final Set<String> citizens = new HashSet<>();

        private void add(ReadingPoint p) {
            total = total.add(p.consumptionKwh());
            readings++;
            citizens.add(p.citizenId());
        }
    }
}
