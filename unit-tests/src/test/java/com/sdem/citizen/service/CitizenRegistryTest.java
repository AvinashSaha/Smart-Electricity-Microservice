package com.sdem.citizen.service;

import com.sdem.citizen.entities.Citizen;
import com.sdem.citizen.entities.MeterType;
import com.sdem.citizen.repository.CitizenRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CitizenRegistryTest {

    @Test
    void buildsReadableIds() {
        assertThat(CitizenRegistry.citizenId("newcastle", "a", MeterType.MANUAL, 7)).isEqualTo("NEWCASTLE-A-M007");
        assertThat(CitizenRegistry.citizenId("durham", "c", MeterType.SMART, 50)).isEqualTo("DURHAM-C-S050");
    }

    @Test
    @SuppressWarnings("unchecked")
    void createsManualCitizensAndSmartMetersForEveryProvider() {
        CitizenRepository repository = mock(CitizenRepository.class);
        when(repository.existsById(anyString())).thenReturn(false);
        CitizenRegistry registry = new CitizenRegistry(repository, TestData.properties(100, 50, 12));

        registry.createMissingCitizens();

        ArgumentCaptor<List<Citizen>> saved = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(saved.capture());
        assertThat(saved.getValue()).hasSize(450);
        assertThat(saved.getValue()).filteredOn(c -> c.getMeterType() == MeterType.SMART).hasSize(150);
    }
}
