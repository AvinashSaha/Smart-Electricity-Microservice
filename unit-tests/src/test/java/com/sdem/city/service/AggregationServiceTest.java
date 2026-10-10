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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AggregationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-10T12:00:00Z"), ZoneId.of("Europe/London"));

    private ProviderClient client;
    private AggregatedConsumptionRepository repository;
    private AggregationService service;
    private final List<AggregatedConsumption> saved = new ArrayList<>();

    @BeforeEach
    void setUp() {
        client = mock(ProviderClient.class);
        repository = mock(AggregatedConsumptionRepository.class);
        when(repository.findByProviderAndMonth(anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.save(any(AggregatedConsumption.class))).thenAnswer(inv -> {
            saved.add(inv.getArgument(0));
            return inv.getArgument(0);
        });
        service = new AggregationService(client, repository,
                new SmartCityProperties("newcastle", List.of("a", "b", "c"), 2), CLOCK);
    }

    @Test
    void collectsAndStoresTotalsFromEveryProvider() {
        when(client.monthly(anyString(), eq(2))).thenReturn(List.of(
                new ProviderMonthly("2026-09", new BigDecimal("1000"), 300, 150),
                new ProviderMonthly("2026-10", new BigDecimal("250"), 90, 60)));

        List<ProviderStatus> statuses = service.collectAll();

        assertThat(statuses).hasSize(3).allMatch(ProviderStatus::reachable);
        assertThat(saved).hasSize(6);
        assertThat(saved).extracting(AggregatedConsumption::getProvider).containsOnly("A", "B", "C");
    }

    @Test
    void keepsGoingWhenOneProviderIsDown() {
        when(client.monthly(eq("provider-newcastle-a"), eq(2)))
                .thenReturn(List.of(new ProviderMonthly("2026-10", BigDecimal.TEN, 1, 1)));
        when(client.monthly(eq("provider-newcastle-b"), eq(2)))
                .thenThrow(new ProviderUnavailableException("down", null));
        when(client.monthly(eq("provider-newcastle-c"), eq(2)))
                .thenReturn(List.of(new ProviderMonthly("2026-10", BigDecimal.ONE, 1, 1)));

        List<ProviderStatus> statuses = service.collectAll();

        assertThat(statuses).filteredOn(s -> !s.reachable()).singleElement()
                .extracting(ProviderStatus::provider).isEqualTo("B");
        assertThat(saved).hasSize(2);
    }

    @Test
    void updatesAnExistingMonthInsteadOfAddingAnother() {
        AggregatedConsumption existing = new AggregatedConsumption("A", "2026-10");
        when(repository.findByProviderAndMonth("A", "2026-10")).thenReturn(Optional.of(existing));
        when(client.monthly(anyString(), eq(2))).thenReturn(List.of(
                new ProviderMonthly("2026-10", new BigDecimal("77"), 7, 7)));

        service.collectAll();

        assertThat(existing.getTotalKwh()).isEqualByComparingTo("77");
    }

    @Test
    void summarySumsStoredTotalsPerProvider() {
        AggregatedConsumption a1 = row("A", "2026-09", "100");
        AggregatedConsumption a2 = row("A", "2026-10", "50");
        AggregatedConsumption b1 = row("B", "2026-10", "30");
        when(repository.findByMonthInOrderByMonthAscProviderAsc(anyCollection())).thenReturn(List.of(a1, a2, b1));

        CitySummary summary = service.summary(2);

        assertThat(summary.totalKwh()).isEqualByComparingTo("180");
        assertThat(summary.providers().get(0).totalKwh()).isEqualByComparingTo("150");
        assertThat(summary.providers().get(2).totalKwh()).isEqualByComparingTo("0");
    }

    @Test
    void aggregatesAreLabelledWithTheCity() {
        when(repository.findByMonthInOrderByMonthAscProviderAsc(anyCollection()))
                .thenReturn(List.of(row("C", "2026-10", "9")));

        List<AggregateRow> rows = service.aggregates(1);

        assertThat(rows).singleElement().satisfies(r -> {
            assertThat(r.city()).isEqualTo("Newcastle");
            assertThat(r.provider()).isEqualTo("C");
        });
    }

    @Test
    void rejectsSillyMonthCounts() {
        assertThatThrownBy(() -> service.aggregates(0)).isInstanceOf(IllegalArgumentException.class);
    }

    private static AggregatedConsumption row(String provider, String month, String kwh) {
        AggregatedConsumption a = new AggregatedConsumption(provider, month);
        a.update(new BigDecimal(kwh), 1, 1, null);
        return a;
    }
}
