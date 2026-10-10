package com.sdem.city.service;

import com.sdem.city.bean.AggregateRow;
import com.sdem.city.bean.CitySummary;
import com.sdem.city.bean.ProviderMonthly;
import com.sdem.city.bean.ProviderStatus;
import com.sdem.city.client.ProviderClient;
import com.sdem.city.config.SmartCityProperties;
import com.sdem.city.entities.AggregatedConsumption;
import com.sdem.city.exception.ProviderUnavailableException;
import com.sdem.city.repository.AggregatedConsumptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects monthly data from every provider of the city, collates it and
 * stores the aggregated totals. Runs on a schedule and on demand.
 */
@Service
public class AggregationService {

    private static final Logger LOG = LoggerFactory.getLogger(AggregationService.class);

    private final ProviderClient client;
    private final AggregatedConsumptionRepository repository;
    private final SmartCityProperties properties;
    private final Clock clock;
    private final Map<String, ProviderStatus> statuses = new ConcurrentHashMap<>();

    public AggregationService(ProviderClient client, AggregatedConsumptionRepository repository,
                              SmartCityProperties properties, Clock clock) {
        this.client = client;
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    /** Scheduled collection. The interval is set by sdem.city.collect-interval (default 30 seconds). */
    @Scheduled(initialDelayString = "${sdem.city.collect-initial-delay:PT20S}",
            fixedDelayString = "${sdem.city.collect-interval:PT30S}")
    public void scheduledCollect() {
        collectAll();
    }

    /**
     * Requests the latest monthly totals from every provider and upserts them.
     * A provider that cannot be reached is recorded as unreachable and skipped,
     * so its previously stored data stays available.
     *
     * @return status of every provider after this run
     */
    @Transactional
    public List<ProviderStatus> collectAll() {
        List<ProviderStatus> result = new ArrayList<>();
        for (String code : properties.providers()) {
            result.add(collect(code));
        }
        return result;
    }

    private ProviderStatus collect(String code) {
        String provider = code.toUpperCase(Locale.UK);
        String serviceId = properties.providerServiceId(code);
        LocalDateTime now = LocalDateTime.now(clock);
        ProviderStatus previous = statuses.get(provider);
        ProviderStatus status;
        try {
            List<ProviderMonthly> months = client.monthly(serviceId, properties.months());
            months.forEach(m -> store(provider, m, now));
            status = new ProviderStatus(provider, serviceId, true, now, now, null);
        } catch (ProviderUnavailableException ex) {
            LOG.warn("Collection from {} failed: {}", serviceId, ex.getMessage());
            status = new ProviderStatus(provider, serviceId, false, now,
                    previous != null ? previous.lastSuccessAt() : null, ex.getMessage());
        }
        statuses.put(provider, status);
        return status;
    }

    private void store(String provider, ProviderMonthly m, LocalDateTime now) {
        AggregatedConsumption row = repository.findByProviderAndMonth(provider, m.month())
                .orElseGet(() -> new AggregatedConsumption(provider, m.month()));
        row.update(m.totalKwh(), m.readingCount(), m.citizenCount(), now);
        repository.save(row);
    }

    /** Stored aggregates for the last {@code months} months, oldest first. */
    @Transactional(readOnly = true)
    public List<AggregateRow> aggregates(int months) {
        if (months < 1 || months > 60) {
            throw new IllegalArgumentException("months must be between 1 and 60");
        }
        YearMonth current = YearMonth.now(clock);
        List<String> range = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            range.add(current.minusMonths(i).toString());
        }
        return repository.findByMonthInOrderByMonthAscProviderAsc(range).stream()
                .map(a -> AggregateRow.from(properties.displayCity(), a))
                .toList();
    }

    /** City totals per provider over the stored months. */
    @Transactional(readOnly = true)
    public CitySummary summary(int months) {
        List<AggregateRow> rows = aggregates(months);
        List<CitySummary.ProviderTotal> totals = new ArrayList<>();
        BigDecimal cityTotal = BigDecimal.ZERO;
        for (String code : properties.providers()) {
            String provider = code.toUpperCase(Locale.UK);
            BigDecimal kwh = BigDecimal.ZERO;
            long readings = 0;
            for (AggregateRow r : rows) {
                if (r.provider().equals(provider)) {
                    kwh = kwh.add(r.totalKwh());
                    readings += r.readingCount();
                }
            }
            cityTotal = cityTotal.add(kwh);
            totals.add(new CitySummary.ProviderTotal(provider, kwh, readings, statuses.get(provider)));
        }
        return new CitySummary(properties.displayCity(), cityTotal, totals);
    }

    public List<ProviderStatus> statuses() {
        return List.copyOf(statuses.values());
    }
}
