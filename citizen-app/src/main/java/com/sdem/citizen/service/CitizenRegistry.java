package com.sdem.citizen.service;

import com.sdem.citizen.config.SimulationProperties;
import com.sdem.citizen.entities.Citizen;
import com.sdem.citizen.entities.MeterType;
import com.sdem.citizen.repository.CitizenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.random.RandomGenerator;

/**
 * Creates the simulated citizens and smart meters the first time the service
 * starts. On later starts the existing ones are loaded from the database.
 */
@Component
public class CitizenRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(CitizenRegistry.class);
    private static final BigDecimal MIN_OPENING = new BigDecimal("1000");
    private static final BigDecimal MAX_OPENING = new BigDecimal("20000");

    private final CitizenRepository repository;
    private final SimulationProperties properties;

    public CitizenRegistry(CitizenRepository repository, SimulationProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void createMissingCitizens() {
        List<Citizen> created = new ArrayList<>();
        RandomGenerator random = RandomGenerator.getDefault();
        for (String code : properties.providers()) {
            created.addAll(createMissing(code, MeterType.MANUAL, properties.manualPerProvider(), random));
            created.addAll(createMissing(code, MeterType.SMART, properties.smartPerProvider(), random));
        }
        repository.saveAll(created);
        LOG.info("{}: {} citizens and smart meters registered ({} new)", properties.city(), repository.count(),
                created.size());
    }

    private List<Citizen> createMissing(String code, MeterType type, int count, RandomGenerator random) {
        List<Citizen> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String id = citizenId(properties.city(), code, type, i);
            if (!repository.existsById(id)) {
                list.add(new Citizen(id, code.toUpperCase(Locale.UK), type,
                        ReadingGenerator.between(MIN_OPENING, MAX_OPENING, random)));
            }
        }
        return list;
    }

    /** Builds ids such as NEWCASTLE-A-M001 (manual) and NEWCASTLE-A-S001 (smart). */
    public static String citizenId(String city, String providerCode, MeterType type, int number) {
        char kind = type == MeterType.MANUAL ? 'M' : 'S';
        return String.format(Locale.UK, "%s-%s-%c%03d", city.toUpperCase(Locale.UK),
                providerCode.toUpperCase(Locale.UK), kind, number);
    }
}
