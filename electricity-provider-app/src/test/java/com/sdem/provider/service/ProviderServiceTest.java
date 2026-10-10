package com.sdem.provider.service;

import com.sdem.provider.bean.BillQuote;
import com.sdem.provider.bean.ProviderRequest;
import com.sdem.provider.entities.Provider;
import com.sdem.provider.exception.DuplicateProviderException;
import com.sdem.provider.exception.ProviderNotFoundException;
import com.sdem.provider.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProviderServiceTest {

    private ProviderRepository repository;
    private ProviderService service;

    @BeforeEach
    void setUp() {
        repository = mock(ProviderRepository.class);
        service = new ProviderService(repository);
    }

    @Test
    void calculatesEnergyStandingChargeAndTotal() {
        Provider provider = new Provider("Test Energy", new BigDecimal("25.00"), new BigDecimal("50.00"), 40);
        provider.setId(1L);

        BillQuote quote = ProviderService.calculate(provider, new BigDecimal("200"), 30);

        assertThat(quote.energyCost()).isEqualByComparingTo("50.00");      // 200 x 25p
        assertThat(quote.standingCharge()).isEqualByComparingTo("15.00");  // 30 x 50p
        assertThat(quote.total()).isEqualByComparingTo("65.00");
        assertThat(quote.renewableKwh()).isEqualByComparingTo("80.00");    // 40% of 200
    }

    @Test
    void rejectsNegativeConsumption() {
        Provider provider = new Provider("Test Energy", BigDecimal.ONE, BigDecimal.ONE, 0);
        assertThatThrownBy(() -> ProviderService.calculate(provider, new BigDecimal("-1"), 30))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwsWhenProviderMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(ProviderNotFoundException.class);
    }

    @Test
    void refusesDuplicateNames() {
        when(repository.existsByNameIgnoreCase("GreenGrid Energy")).thenReturn(true);
        ProviderRequest request = new ProviderRequest("GreenGrid Energy", BigDecimal.TEN, BigDecimal.TEN, 50);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(DuplicateProviderException.class);
    }

    @Test
    void createsProvider() {
        when(repository.existsByNameIgnoreCase(any())).thenReturn(false);
        when(repository.save(any(Provider.class))).thenAnswer(inv -> inv.getArgument(0));
        Provider saved = service.create(new ProviderRequest("New Co", BigDecimal.TEN, BigDecimal.ONE, 10));
        assertThat(saved.getName()).isEqualTo("New Co");
        assertThat(saved.isActive()).isTrue();
    }
}
