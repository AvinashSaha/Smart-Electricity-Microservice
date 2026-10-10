package com.sdem.citizen.service;

import com.sdem.citizen.bean.ReadingPayload;
import com.sdem.citizen.client.ProviderClient;
import com.sdem.citizen.entities.Citizen;
import com.sdem.citizen.entities.MeterType;
import com.sdem.citizen.exception.InvalidReadingException;
import com.sdem.citizen.exception.ProviderUnavailableException;
import com.sdem.citizen.repository.CitizenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReadingSenderTest {

    private ProviderClient client;
    private CitizenRepository repository;
    private ReadingSender sender;
    private Citizen citizen;

    @BeforeEach
    void setUp() {
        client = mock(ProviderClient.class);
        repository = mock(CitizenRepository.class);
        when(repository.save(any(Citizen.class))).thenAnswer(inv -> inv.getArgument(0));
        sender = new ReadingSender(client, repository, TestData.properties(1, 1, 1));
        citizen = new Citizen("NEWCASTLE-B-M001", "B", MeterType.MANUAL, new BigDecimal("500"));
        citizen.accept(new BigDecimal("500"), LocalDateTime.of(2026, 10, 1, 8, 0));
    }

    @Test
    void sendsToTheContractedProviderInRealTime() {
        sender.send(citizen, new BigDecimal("510"), LocalDateTime.of(2026, 10, 2, 8, 0));

        ArgumentCaptor<ReadingPayload> payload = ArgumentCaptor.forClass(ReadingPayload.class);
        verify(client).send(org.mockito.ArgumentMatchers.eq("provider-newcastle-b"), payload.capture());
        assertThat(payload.getValue().readingKwh()).isEqualByComparingTo("510");
        assertThat(citizen.getLastReadingKwh()).isEqualByComparingTo("510");
    }

    @Test
    void refusesAReadingThatIsNotHigher() {
        assertThatThrownBy(() -> sender.send(citizen, new BigDecimal("499"), LocalDateTime.now()))
                .isInstanceOf(InvalidReadingException.class);
        verify(client, never()).send(anyString(), any());
    }

    @Test
    void keepsTheOldReadingWhenTheProviderIsDown() {
        doThrow(new ProviderUnavailableException("down", null)).when(client).send(anyString(), any());

        assertThatThrownBy(() -> sender.send(citizen, new BigDecimal("600"), LocalDateTime.now()))
                .isInstanceOf(ProviderUnavailableException.class);
        assertThat(citizen.getLastReadingKwh()).isEqualByComparingTo("500");
    }
}
