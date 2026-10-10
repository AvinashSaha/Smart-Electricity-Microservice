package com.sdem.citizen.service;

import com.sdem.citizen.bean.ConsumptionSummary;
import com.sdem.citizen.bean.ReadingRequest;
import com.sdem.citizen.entities.Citizen;
import com.sdem.citizen.entities.MeterReading;
import com.sdem.citizen.repository.CitizenRepository;
import com.sdem.citizen.repository.MeterReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CitizenServiceTest {

    private CitizenRepository citizens;
    private MeterReadingRepository readings;
    private CitizenService service;
    private Citizen citizen;

    @BeforeEach
    void setUp() {
        citizens = mock(CitizenRepository.class);
        readings = mock(MeterReadingRepository.class);
        service = new CitizenService(citizens, readings, mock(ProviderClient.class));
        citizen = new Citizen("Test Person", "test@example.com", "NE1 1AA", "SM-1", 1L);
        citizen.setId(1L);
    }

    @Test
    void consumptionIsDifferenceBetweenFirstAndLastReading() {
        List<MeterReading> ordered = List.of(
                new MeterReading(new BigDecimal("1000.00"), LocalDateTime.of(2026, 9, 1, 8, 0)),
                new MeterReading(new BigDecimal("1100.00"), LocalDateTime.of(2026, 9, 11, 8, 0)),
                new MeterReading(new BigDecimal("1250.50"), LocalDateTime.of(2026, 10, 1, 8, 0)));

        ConsumptionSummary summary = CitizenService.summarise(citizen, ordered);

        assertThat(summary.kwh()).isEqualByComparingTo("250.50");
        assertThat(summary.days()).isEqualTo(30);
        assertThat(summary.readingCount()).isEqualTo(3);
    }

    @Test
    void consumptionIsZeroWithASingleReading() {
        ConsumptionSummary summary = CitizenService.summarise(citizen,
                List.of(new MeterReading(BigDecimal.TEN, LocalDateTime.now())));
        assertThat(summary.kwh()).isEqualByComparingTo("0");
        assertThat(summary.days()).isZero();
    }

    @Test
    void rejectsReadingLowerThanPrevious() {
        when(citizens.findById(1L)).thenReturn(Optional.of(citizen));
        when(readings.findFirstByCitizenIdOrderByReadingAtDesc(1L)).thenReturn(Optional.of(
                new MeterReading(new BigDecimal("500"), LocalDateTime.now().minusDays(1))));

        ReadingRequest lower = new ReadingRequest(new BigDecimal("400"), LocalDateTime.now());
        assertThatThrownBy(() -> service.addReading(1L, lower))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lower than the previous reading");
    }

    @Test
    void normalisesPostcodes() {
        assertThat(CitizenService.normalisePostcode("ne45ab")).isEqualTo("NE4 5AB");
        assertThat(CitizenService.normalisePostcode("sw1a 1aa")).isEqualTo("SW1A 1AA");
    }
}
