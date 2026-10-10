package com.sdem.provider.service;

import com.sdem.provider.bean.ReadingRequest;
import com.sdem.provider.config.ProviderProperties;
import com.sdem.provider.entities.MeterReading;
import com.sdem.provider.entities.MeterType;
import com.sdem.provider.exception.InvalidReadingException;
import com.sdem.provider.repository.MeterReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReadingServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/London");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-10T12:00:00Z"), ZONE);

    private MeterReadingRepository repository;
    private ReadingService service;

    @BeforeEach
    void setUp() {
        repository = mock(MeterReadingRepository.class);
        when(repository.save(any(MeterReading.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new ReadingService(repository, new ProviderProperties("newcastle", "a"), CLOCK);
    }

    @Test
    void firstReadingHasZeroConsumption() {
        when(repository.findTopByCitizenIdOrderByRecordedAtDesc("C1")).thenReturn(Optional.empty());

        MeterReading saved = service.receive(new ReadingRequest("C1", MeterType.SMART, new BigDecimal("1000"), null));

        assertThat(saved.getConsumptionKwh()).isEqualByComparingTo("0");
        assertThat(saved.getRecordedAt()).isEqualTo(LocalDateTime.now(CLOCK));
    }

    @Test
    void consumptionIsTheDifferenceFromThePreviousReading() {
        when(repository.findTopByCitizenIdOrderByRecordedAtDesc("C1")).thenReturn(Optional.of(previous("1000.000")));

        MeterReading saved = service.receive(new ReadingRequest("C1", MeterType.MANUAL, new BigDecimal("1012.250"),
                LocalDateTime.of(2026, 10, 10, 9, 0)));

        assertThat(saved.getConsumptionKwh()).isEqualByComparingTo("12.250");
    }

    @Test
    void rejectsReadingThatIsNotHigher() {
        when(repository.findTopByCitizenIdOrderByRecordedAtDesc("C1")).thenReturn(Optional.of(previous("1000")));

        ReadingRequest same = new ReadingRequest("C1", MeterType.SMART, new BigDecimal("1000"),
                LocalDateTime.of(2026, 10, 10, 9, 0));

        assertThatThrownBy(() -> service.receive(same))
                .isInstanceOf(InvalidReadingException.class)
                .hasMessageContaining("must be more than the previous reading");
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsReadingThatIsNotLater() {
        when(repository.findTopByCitizenIdOrderByRecordedAtDesc("C1")).thenReturn(Optional.of(previous("1000")));

        ReadingRequest earlier = new ReadingRequest("C1", MeterType.SMART, new BigDecimal("1001"),
                LocalDateTime.of(2026, 10, 1, 0, 0));

        assertThatThrownBy(() -> service.receive(earlier)).isInstanceOf(InvalidReadingException.class);
    }

    @Test
    void summaryUsesDisplayNames() {
        when(repository.count()).thenReturn(5L);
        when(repository.countCitizens()).thenReturn(2L);
        when(repository.totalConsumption()).thenReturn(new BigDecimal("42"));

        assertThat(service.summary().city()).isEqualTo("Newcastle");
        assertThat(service.summary().provider()).isEqualTo("A");
    }

    private static MeterReading previous(String kwh) {
        return new MeterReading("C1", MeterType.SMART, new BigDecimal(kwh), BigDecimal.ZERO,
                LocalDateTime.of(2026, 10, 5, 8, 0), LocalDateTime.of(2026, 10, 5, 8, 0));
    }
}
