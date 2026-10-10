package com.sdem.citizen.service;

import com.sdem.citizen.bean.CitizenBill;
import com.sdem.citizen.bean.CitizenRequest;
import com.sdem.citizen.bean.ConsumptionSummary;
import com.sdem.citizen.bean.ProviderQuote;
import com.sdem.citizen.bean.ReadingRequest;
import com.sdem.citizen.entities.Citizen;
import com.sdem.citizen.entities.MeterReading;
import com.sdem.citizen.exception.CitizenNotFoundException;
import com.sdem.citizen.exception.ConflictException;
import com.sdem.citizen.repository.CitizenRepository;
import com.sdem.citizen.repository.MeterReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class CitizenService {

    private final CitizenRepository citizens;
    private final MeterReadingRepository readings;
    private final ProviderClient providerClient;

    public CitizenService(CitizenRepository citizens, MeterReadingRepository readings, ProviderClient providerClient) {
        this.citizens = citizens;
        this.readings = readings;
        this.providerClient = providerClient;
    }

    @Transactional(readOnly = true)
    public List<Citizen> findAll() {
        return citizens.findAll();
    }

    @Transactional(readOnly = true)
    public Citizen findById(Long id) {
        return citizens.findById(id).orElseThrow(() -> new CitizenNotFoundException(id));
    }

    public Citizen register(CitizenRequest request) {
        if (citizens.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email " + request.email() + " is already registered");
        }
        if (citizens.existsByMeterId(request.meterId())) {
            throw new ConflictException("Meter " + request.meterId() + " is already registered");
        }
        // Make sure the chosen provider really exists in the provider service
        providerClient.getProvider(request.providerId());
        Citizen citizen = new Citizen(request.fullName(), request.email(),
                normalisePostcode(request.postcode()), request.meterId(), request.providerId());
        return citizens.save(citizen);
    }

    public Citizen update(Long id, CitizenRequest request) {
        Citizen citizen = findById(id);
        if (!citizen.getProviderId().equals(request.providerId())) {
            providerClient.getProvider(request.providerId());
        }
        citizen.setFullName(request.fullName());
        citizen.setEmail(request.email());
        citizen.setPostcode(normalisePostcode(request.postcode()));
        citizen.setMeterId(request.meterId());
        citizen.setProviderId(request.providerId());
        return citizens.save(citizen);
    }

    public void delete(Long id) {
        citizens.delete(findById(id));
    }

    /** Switch supplier after checking the new provider exists. */
    public Citizen switchProvider(Long id, Long providerId) {
        Citizen citizen = findById(id);
        providerClient.getProvider(providerId);
        citizen.setProviderId(providerId);
        return citizens.save(citizen);
    }

    public MeterReading addReading(Long citizenId, ReadingRequest request) {
        Citizen citizen = findById(citizenId);
        LocalDateTime at = request.readingAt() != null ? request.readingAt() : LocalDateTime.now();
        readings.findFirstByCitizenIdOrderByReadingAtDesc(citizenId).ifPresent(latest -> {
            if (request.readingKwh().compareTo(latest.getReadingKwh()) < 0) {
                throw new IllegalArgumentException("Reading " + request.readingKwh()
                        + " kWh is lower than the previous reading of " + latest.getReadingKwh() + " kWh");
            }
            if (!at.isAfter(latest.getReadingAt())) {
                throw new IllegalArgumentException("Reading time must be after the previous reading at "
                        + latest.getReadingAt());
            }
        });
        MeterReading reading = new MeterReading(request.readingKwh(), at);
        citizen.addReading(reading);
        return readings.save(reading);
    }

    @Transactional(readOnly = true)
    public List<MeterReading> readingsFor(Long citizenId) {
        findById(citizenId);
        return readings.findByCitizenIdOrderByReadingAtAsc(citizenId);
    }

    @Transactional(readOnly = true)
    public ConsumptionSummary consumption(Long citizenId) {
        return summarise(findById(citizenId), readings.findByCitizenIdOrderByReadingAtAsc(citizenId));
    }

    /** Consumption for every citizen; used by the Smart City service for city wide reports. */
    @Transactional(readOnly = true)
    public List<ConsumptionSummary> consumptionForAll() {
        return citizens.findAll().stream()
                .map(c -> summarise(c, readings.findByCitizenIdOrderByReadingAtAsc(c.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public CitizenBill bill(Long citizenId) {
        Citizen citizen = findById(citizenId);
        ConsumptionSummary usage = summarise(citizen, readings.findByCitizenIdOrderByReadingAtAsc(citizenId));
        ProviderQuote quote = providerClient.quote(citizen.getProviderId(), usage.kwh(), usage.days());
        return new CitizenBill(citizen.getId(), citizen.getFullName(), usage, quote);
    }

    static ConsumptionSummary summarise(Citizen citizen, List<MeterReading> ordered) {
        if (ordered.size() < 2) {
            LocalDateTime only = ordered.isEmpty() ? null : ordered.get(0).getReadingAt();
            return new ConsumptionSummary(citizen.getId(), citizen.getPostcode(), citizen.getProviderId(),
                    ordered.size(), only, only, 0, BigDecimal.ZERO);
        }
        MeterReading first = ordered.get(0);
        MeterReading last = ordered.get(ordered.size() - 1);
        BigDecimal kwh = last.getReadingKwh().subtract(first.getReadingKwh());
        long hours = Duration.between(first.getReadingAt(), last.getReadingAt()).toHours();
        int days = (int) Math.max(1, Math.ceil(hours / 24.0));
        return new ConsumptionSummary(citizen.getId(), citizen.getPostcode(), citizen.getProviderId(),
                ordered.size(), first.getReadingAt(), last.getReadingAt(), days, kwh);
    }

    static String normalisePostcode(String postcode) {
        String compact = postcode.replace(" ", "").toUpperCase(Locale.UK);
        return compact.substring(0, compact.length() - 3) + " " + compact.substring(compact.length() - 3);
    }
}
