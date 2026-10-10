package com.sdem.citizen.service;

import com.sdem.citizen.entities.Citizen;
import com.sdem.citizen.entities.MeterType;
import com.sdem.citizen.repository.CitizenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SimulationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-10T12:00:00Z"), ZoneId.of("Europe/London"));

    private CitizenRepository repository;
    private ReadingSender sender;
    private SimulationService simulation;

    @BeforeEach
    void setUp() {
        repository = mock(CitizenRepository.class);
        sender = mock(ReadingSender.class);
        simulation = new SimulationService(repository, sender, TestData.properties(2, 2, 12), CLOCK);
    }

    @Test
    void historyHasOneReadingPerMonthAndAlwaysIncreases() {
        Citizen citizen = new Citizen("NEWCASTLE-A-M001", "A", MeterType.MANUAL, new BigDecimal("5000"));

        List<SimulationService.HistoryPoint> history = simulation.historyFor(citizen);

        // opening reading + 12 months + month to date
        assertThat(history).hasSize(14);
        for (int i = 1; i < history.size(); i++) {
            assertThat(history.get(i).readingKwh()).isGreaterThan(history.get(i - 1).readingKwh());
            assertThat(history.get(i).at()).isAfter(history.get(i - 1).at());
        }
        assertThat(history.get(history.size() - 1).at()).isBefore(LocalDateTime.now(CLOCK));
    }

    @Test
    void everyBackfilledSmartMeterSendsAHigherReading() {
        Citizen meter1 = backfilledMeter("NEWCASTLE-A-S001");
        Citizen meter2 = backfilledMeter("NEWCASTLE-A-S002");
        Citizen notReady = new Citizen("NEWCASTLE-A-S003", "A", MeterType.SMART, new BigDecimal("10"));
        when(repository.findByMeterTypeOrderById(MeterType.SMART)).thenReturn(List.of(meter1, meter2, notReady));

        simulation.sendSmartMeterReadings();

        verify(sender, times(2)).send(any(Citizen.class), any(BigDecimal.class), any(LocalDateTime.class));
    }

    @Test
    void statusReportsCounts() {
        when(repository.countByMeterType(MeterType.MANUAL)).thenReturn(300L);
        when(repository.countByMeterType(MeterType.SMART)).thenReturn(150L);

        assertThat(simulation.status().manualCitizens()).isEqualTo(300);
        assertThat(simulation.status().smartMeters()).isEqualTo(150);
    }

    private static Citizen backfilledMeter(String id) {
        Citizen c = new Citizen(id, "A", MeterType.SMART, new BigDecimal("100"));
        c.accept(new BigDecimal("100"), LocalDateTime.of(2026, 10, 10, 12, 0));
        c.markBackfilled();
        return c;
    }
}
