package com.sdem.city.service;

import com.sdem.city.bean.CitizenUsage;
import com.sdem.city.bean.CitySummary;
import com.sdem.city.bean.ProviderInfo;
import com.sdem.city.repository.CitySnapshotRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CityServiceTest {

    private final CityService service = new CityService(mock(DownstreamClient.class),
            mock(CitySnapshotRepository.class), new BigDecimal("0.2"));

    private final List<ProviderInfo> providers = List.of(
            new ProviderInfo(1L, "Green", BigDecimal.TEN, BigDecimal.ONE, 100, true),
            new ProviderInfo(2L, "Grey", BigDecimal.TEN, BigDecimal.ONE, 0, true));

    @Test
    void aggregatesTotalsRenewablesAndCarbon() {
        List<CitizenUsage> usage = List.of(
                new CitizenUsage(1L, "NE4 5AB", 1L, 30, new BigDecimal("100")),
                new CitizenUsage(2L, "NE4 9QR", 2L, 30, new BigDecimal("300")),
                new CitizenUsage(3L, "NE1 4ST", 1L, 30, new BigDecimal("200")));

        CitySummary summary = service.summarise(usage, providers);

        assertThat(summary.citizenCount()).isEqualTo(3);
        assertThat(summary.totalKwh()).isEqualByComparingTo("600");
        assertThat(summary.averageKwhPerCitizen()).isEqualByComparingTo("200");
        assertThat(summary.renewableKwh()).isEqualByComparingTo("300");
        assertThat(summary.renewableSharePercent()).isEqualByComparingTo("50.0");
        assertThat(summary.co2AvoidedKg()).isEqualByComparingTo("60.00");
    }

    @Test
    void groupsByDistrictAndProvider() {
        List<CitizenUsage> usage = List.of(
                new CitizenUsage(1L, "NE4 5AB", 1L, 30, new BigDecimal("100")),
                new CitizenUsage(2L, "NE4 9QR", 2L, 30, new BigDecimal("300")),
                new CitizenUsage(3L, "NE1 4ST", 1L, 30, new BigDecimal("200")));

        CitySummary summary = service.summarise(usage, providers);

        assertThat(summary.byDistrict()).extracting("district").containsExactly("NE1", "NE4");
        assertThat(summary.byDistrict().get(1).kwh()).isEqualByComparingTo("400");
        assertThat(summary.byProvider()).hasSize(2);
        assertThat(summary.byProvider().get(0).kwh()).isEqualByComparingTo("300");
    }

    @Test
    void handlesAnEmptyCity() {
        CitySummary summary = service.summarise(List.of(), providers);
        assertThat(summary.totalKwh()).isEqualByComparingTo("0");
        assertThat(summary.renewableSharePercent()).isEqualByComparingTo("0");
        assertThat(summary.byDistrict()).isEmpty();
    }

    @Test
    void extractsDistrictFromPostcode() {
        assertThat(CityService.district("ne4 5ab")).isEqualTo("NE4");
        assertThat(CityService.district("SW1A1AA")).isEqualTo("SW1A");
        assertThat(CityService.district(null)).isEqualTo("UNKNOWN");
    }
}
