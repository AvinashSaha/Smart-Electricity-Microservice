package com.sdem.citizen.service;

import com.sdem.citizen.entities.Citizen;
import com.sdem.citizen.entities.MeterType;
import com.sdem.citizen.exception.CitizenNotFoundException;
import com.sdem.citizen.exception.InvalidReadingException;
import com.sdem.citizen.repository.CitizenRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CitizenServiceTest {

    private final CitizenRepository repository = mock(CitizenRepository.class);
    private final CitizenService service = new CitizenService(repository, mock(ReadingSender.class),
            Clock.systemDefaultZone());

    @Test
    void smartMetersCannotBeReadByHand() {
        when(repository.findById("NEWCASTLE-A-S001")).thenReturn(Optional.of(
                new Citizen("NEWCASTLE-A-S001", "A", MeterType.SMART, BigDecimal.TEN)));

        assertThatThrownBy(() -> service.submitManual("NEWCASTLE-A-S001", new BigDecimal("20")))
                .isInstanceOf(InvalidReadingException.class);
    }

    @Test
    void unknownCitizenIsReported() {
        when(repository.findById("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get("NOPE")).isInstanceOf(CitizenNotFoundException.class);
    }
}
