package com.sdem.city.service;

import com.sdem.city.bean.CitizenUsage;
import com.sdem.city.bean.CitySummary;
import com.sdem.city.bean.DistrictUsage;
import com.sdem.city.bean.ProviderInfo;
import com.sdem.city.bean.ProviderUsage;
import com.sdem.city.entities.CitySnapshot;
import com.sdem.city.repository.CitySnapshotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CityService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final DownstreamClient client;
    private final CitySnapshotRepository snapshots;
    private final BigDecimal gridCarbonKgPerKwh;

    public CityService(DownstreamClient client,
                       CitySnapshotRepository snapshots,
                       @Value("${sdem.grid-carbon-kg-per-kwh:0.207}") BigDecimal gridCarbonKgPerKwh) {
        this.client = client;
        this.snapshots = snapshots;
        this.gridCarbonKgPerKwh = gridCarbonKgPerKwh;
    }

    /** Builds the live city report from the Citizen and Provider services. */
    public CitySummary summary() {
        return summarise(client.citizenUsage(), client.providers());
    }

    CitySummary summarise(List<CitizenUsage> usage, List<ProviderInfo> providers) {
        Map<Long, ProviderInfo> providerById = providers.stream()
                .collect(Collectors.toMap(ProviderInfo::id, Function.identity()));

        BigDecimal totalKwh = BigDecimal.ZERO;
        BigDecimal renewableKwh = BigDecimal.ZERO;
        for (CitizenUsage u : usage) {
            totalKwh = totalKwh.add(u.kwh());
            renewableKwh = renewableKwh.add(renewablePart(u, providerById));
        }

        long count = usage.size();
        BigDecimal average = count == 0 ? BigDecimal.ZERO
                : totalKwh.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        BigDecimal share = totalKwh.signum() == 0 ? BigDecimal.ZERO
                : renewableKwh.multiply(HUNDRED).divide(totalKwh, 1, RoundingMode.HALF_UP);
        BigDecimal co2 = renewableKwh.multiply(gridCarbonKgPerKwh).setScale(2, RoundingMode.HALF_UP);

        return new CitySummary(LocalDateTime.now(), count, totalKwh.setScale(2, RoundingMode.HALF_UP),
                average, renewableKwh.setScale(2, RoundingMode.HALF_UP), share, co2,
                byProvider(usage, providerById), byDistrict(usage));
    }

    private List<ProviderUsage> byProvider(List<CitizenUsage> usage, Map<Long, ProviderInfo> providerById) {
        Map<Long, List<CitizenUsage>> grouped = usage.stream()
                .collect(Collectors.groupingBy(CitizenUsage::providerId));
        List<ProviderUsage> result = new ArrayList<>();
        grouped.forEach((providerId, list) -> {
            ProviderInfo info = providerById.get(providerId);
            BigDecimal kwh = sum(list);
            BigDecimal renewable = list.stream().map(u -> renewablePart(u, providerById))
                    .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
            result.add(new ProviderUsage(providerId,
                    info != null ? info.name() : "Unknown provider " + providerId,
                    info != null ? info.renewablePercent() : 0,
                    list.size(), kwh, renewable));
        });
        result.sort(Comparator.comparing(ProviderUsage::kwh).reversed());
        return result;
    }

    private List<DistrictUsage> byDistrict(List<CitizenUsage> usage) {
        Map<String, List<CitizenUsage>> grouped = usage.stream()
                .collect(Collectors.groupingBy(u -> district(u.postcode()), TreeMap::new, Collectors.toList()));
        List<DistrictUsage> result = new ArrayList<>();
        grouped.forEach((district, list) -> {
            BigDecimal kwh = sum(list);
            BigDecimal avg = kwh.divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP);
            result.add(new DistrictUsage(district, list.size(), kwh, avg));
        });
        return result;
    }

    @Transactional
    public CitySnapshot takeSnapshot() {
        CitySummary s = summary();
        return snapshots.save(new CitySnapshot(s.generatedAt(), s.citizenCount(), s.totalKwh(),
                s.renewableKwh(), s.co2AvoidedKg()));
    }

    @Transactional(readOnly = true)
    public List<CitySnapshot> history() {
        return snapshots.findAllByOrderByTakenAtDesc();
    }

    private static BigDecimal renewablePart(CitizenUsage u, Map<Long, ProviderInfo> providerById) {
        ProviderInfo info = providerById.get(u.providerId());
        if (info == null) {
            return BigDecimal.ZERO;
        }
        return u.kwh().multiply(BigDecimal.valueOf(info.renewablePercent())).divide(HUNDRED, 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal sum(List<CitizenUsage> list) {
        return list.stream().map(CitizenUsage::kwh).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** "NE4 5AB" becomes "NE4" (the outward code). */
    static String district(String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return "UNKNOWN";
        }
        String trimmed = postcode.trim().toUpperCase();
        int space = trimmed.indexOf(' ');
        return space > 0 ? trimmed.substring(0, space) : trimmed.substring(0, Math.max(1, trimmed.length() - 3));
    }
}
