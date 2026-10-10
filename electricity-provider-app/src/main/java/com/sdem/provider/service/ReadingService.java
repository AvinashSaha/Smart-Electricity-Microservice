package com.sdem.provider.service;

import com.sdem.provider.bean.MonthlyConsumption;
import com.sdem.provider.bean.ProviderSummary;
import com.sdem.provider.bean.ReadingRequest;
import com.sdem.provider.config.ProviderProperties;
import com.sdem.provider.entities.MeterReading;
import com.sdem.provider.exception.InvalidReadingException;
import com.sdem.provider.repository.MeterReadingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/** Business logic for receiving and reporting meter readings. */
@Service
public class ReadingService {

    private final MeterReadingRepository repository;
    private final ProviderProperties properties;
    private final Clock clock;

    public ReadingService(MeterReadingRepository repository, ProviderProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Stores a reading in real time. The reading must be higher than, and
     * later than, the previous reading from the same citizen.
     */
    @Transactional
    public MeterReading receive(ReadingRequest request) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime recordedAt = request.recordedAt() != null ? request.recordedAt() : now;
        Optional<MeterReading> previous = repository.findTopByCitizenIdOrderByRecordedAtDesc(request.citizenId());
        BigDecimal consumption = previous
                .map(p -> validateAgainst(p, request.readingKwh(), recordedAt))
                .orElse(BigDecimal.ZERO);
        return repository.save(new MeterReading(request.citizenId(), request.meterType(), request.readingKwh(),
                consumption, recordedAt, now));
    }

    static BigDecimal validateAgainst(MeterReading previous, BigDecimal readingKwh, LocalDateTime recordedAt) {
        if (readingKwh.compareTo(previous.getReadingKwh()) <= 0) {
            throw new InvalidReadingException("Reading " + readingKwh + " kWh must be more than the previous reading of "
                    + previous.getReadingKwh() + " kWh for " + previous.getCitizenId());
        }
        if (!recordedAt.isAfter(previous.getRecordedAt())) {
            throw new InvalidReadingException("Reading time " + recordedAt + " must be after the previous reading at "
                    + previous.getRecordedAt());
        }
        return readingKwh.subtract(previous.getReadingKwh());
    }

    @Transactional(readOnly = true)
    public List<MeterReading> latest(String citizenId, int limit) {
        PageRequest page = PageRequest.of(0, Math.max(1, Math.min(limit, 500)));
        return citizenId == null || citizenId.isBlank()
                ? repository.findAllByOrderByRecordedAtDesc(page)
                : repository.findByCitizenIdOrderByRecordedAtDesc(citizenId, page);
    }

    /** Monthly totals for the last {@code months} months, including the current month. */
    @Transactional(readOnly = true)
    public List<MonthlyConsumption> monthly(int months) {
        List<YearMonth> range = MonthlyAggregator.lastMonths(YearMonth.now(clock), months);
        LocalDateTime from = range.get(0).atDay(1).atStartOfDay();
        return MonthlyAggregator.aggregate(repository.findPointsSince(from), range);
    }

    @Transactional(readOnly = true)
    public ProviderSummary summary() {
        return new ProviderSummary(properties.displayCity(), properties.displayCode(), repository.count(),
                repository.countCitizens(), repository.totalConsumption(), repository.latestRecordedAt());
    }
}
