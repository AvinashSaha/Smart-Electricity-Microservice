package com.sdem.citizen.service;

import com.sdem.citizen.bean.SimulationStatus;
import com.sdem.citizen.config.SimulationProperties;
import com.sdem.citizen.entities.Citizen;
import com.sdem.citizen.entities.MeterType;
import com.sdem.citizen.exception.InvalidReadingException;
import com.sdem.citizen.exception.ProviderUnavailableException;
import com.sdem.citizen.repository.CitizenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;

/**
 * Drives the simulation. On every tick:
 * <ol>
 *     <li>citizens whose history has not been sent yet send one reading per past month (first run only),</li>
 *     <li>every smart meter sends a new, higher reading,</li>
 *     <li>a random share of the manual citizens enter a new, higher reading.</li>
 * </ol>
 * Delivery failures are counted and retried on the next tick.
 */
@Service
public class SimulationService {

    private static final Logger LOG = LoggerFactory.getLogger(SimulationService.class);

    private final CitizenRepository repository;
    private final ReadingSender sender;
    private final SimulationProperties properties;
    private final Clock clock;
    private final RandomGenerator random = RandomGenerator.getDefault();

    private final AtomicLong ticks = new AtomicLong();
    private final AtomicLong sent = new AtomicLong();
    private final AtomicLong failures = new AtomicLong();
    private volatile LocalDateTime lastTickAt;

    public SimulationService(CitizenRepository repository, ReadingSender sender,
                             SimulationProperties properties, Clock clock) {
        this.repository = repository;
        this.sender = sender;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(initialDelayString = "${sdem.simulation.initial-delay:PT25S}",
            fixedDelayString = "${sdem.simulation.tick-interval:PT30S}")
    public void tick() {
        ticks.incrementAndGet();
        lastTickAt = LocalDateTime.now(clock);
        try {
            backfillHistory();
            sendSmartMeterReadings();
            sendManualReadings();
        } catch (ProviderUnavailableException ex) {
            LOG.warn("{}: provider unavailable, will retry next tick ({})", properties.city(), ex.getMessage());
        }
    }

    /** Sends one historical reading per month for citizens that have not sent their history yet. */
    void backfillHistory() {
        List<Citizen> pending = repository.findByBackfilledFalseOrderById();
        if (pending.isEmpty()) {
            return;
        }
        LOG.info("{}: sending {} months of history for {} citizens", properties.city(),
                properties.backfillMonths(), pending.size());
        for (Citizen citizen : pending) {
            for (HistoryPoint point : historyFor(citizen)) {
                if (citizen.getLastReadingAt() == null || point.at().isAfter(citizen.getLastReadingAt())) {
                    deliver(citizen, point.readingKwh(), point.at(), true);
                }
            }
            citizen.markBackfilled();
            repository.save(citizen);
        }
    }

    void sendSmartMeterReadings() {
        LocalDateTime now = nowToSeconds();
        for (Citizen meter : repository.findByMeterTypeOrderById(MeterType.SMART)) {
            if (!meter.isBackfilled()) {
                continue;
            }
            BigDecimal next = ReadingGenerator.next(meter.getLastReadingKwh(), properties.smartIncrementMin(),
                    properties.smartIncrementMax(), random);
            deliver(meter, next, now, false);
        }
    }

    void sendManualReadings() {
        LocalDateTime now = nowToSeconds();
        for (Citizen citizen : repository.findByMeterTypeOrderById(MeterType.MANUAL)) {
            if (!citizen.isBackfilled() || random.nextDouble() >= properties.manualSubmitChance()) {
                continue;
            }
            BigDecimal next = ReadingGenerator.next(citizen.getLastReadingKwh(), properties.manualIncrementMin(),
                    properties.manualIncrementMax(), random);
            deliver(citizen, next, now, false);
        }
    }

    private void deliver(Citizen citizen, BigDecimal readingKwh, LocalDateTime at, boolean rethrowUnavailable) {
        try {
            sender.send(citizen, readingKwh, at);
            sent.incrementAndGet();
        } catch (InvalidReadingException ex) {
            failures.incrementAndGet();
            LOG.debug("Reading rejected for {}: {}", citizen.getId(), ex.getMessage());
        } catch (ProviderUnavailableException ex) {
            failures.incrementAndGet();
            if (rethrowUnavailable) {
                throw ex;
            }
        }
    }

    /**
     * History for one citizen: an opening reading just before the oldest month,
     * one reading in each of the last {@code backfillMonths} complete months, and a
     * month-to-date reading for the current month. Consumption follows the seasons.
     */
    List<HistoryPoint> historyFor(Citizen citizen) {
        LocalDateTime now = nowToSeconds();
        YearMonth current = YearMonth.from(now);
        double factor = properties.consumptionFactor() * properties.providerFactor(citizen.getProviderCode());
        List<HistoryPoint> points = new ArrayList<>();

        BigDecimal reading = citizen.getLastReadingKwh();
        YearMonth opening = current.minusMonths(properties.backfillMonths() + 1L);
        points.add(new HistoryPoint(reading, opening.atEndOfMonth().atTime(20, 0)));

        for (int i = properties.backfillMonths(); i >= 1; i--) {
            YearMonth month = current.minusMonths(i);
            BigDecimal used = monthlyUsage(month, factor);
            reading = reading.add(used);
            points.add(new HistoryPoint(reading, month.atEndOfMonth().atTime(20, 0)));
        }

        BigDecimal fullMonth = monthlyUsage(current, factor);
        double share = (double) now.getDayOfMonth() / current.lengthOfMonth();
        BigDecimal monthToDate = ReadingGenerator.scale(fullMonth, share).max(new BigDecimal("0.001"));
        points.add(new HistoryPoint(reading.add(monthToDate), now.minusMinutes(5)));
        return points;
    }

    private BigDecimal monthlyUsage(YearMonth month, double factor) {
        BigDecimal base = ReadingGenerator.between(properties.monthlyMinKwh(), properties.monthlyMaxKwh(), random);
        return ReadingGenerator.scale(base, factor * ReadingGenerator.seasonalFactor(month.getMonth()));
    }

    private LocalDateTime nowToSeconds() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
    }

    public SimulationStatus status() {
        return new SimulationStatus(properties.city(), repository.countByMeterType(MeterType.MANUAL),
                repository.countByMeterType(MeterType.SMART), repository.countByBackfilledFalse(),
                ticks.get(), sent.get(), failures.get(), lastTickAt);
    }

    /** A reading value and the time it was taken. */
    record HistoryPoint(BigDecimal readingKwh, LocalDateTime at) {
    }
}
