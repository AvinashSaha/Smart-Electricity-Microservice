package com.sdem.citizen.service;

import com.sdem.citizen.bean.ReadingPayload;
import com.sdem.citizen.client.ProviderClient;
import com.sdem.citizen.config.SimulationProperties;
import com.sdem.citizen.entities.Citizen;
import com.sdem.citizen.exception.InvalidReadingException;
import com.sdem.citizen.repository.CitizenRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sends one reading to the citizen's provider and, only if the provider
 * accepts it, records it as the citizen's latest reading.
 */
@Component
public class ReadingSender {

    private final ProviderClient client;
    private final CitizenRepository repository;
    private final SimulationProperties properties;

    public ReadingSender(ProviderClient client, CitizenRepository repository, SimulationProperties properties) {
        this.client = client;
        this.repository = repository;
        this.properties = properties;
    }

    /**
     * @throws InvalidReadingException if the reading is not higher than the citizen's last reading
     */
    public Citizen send(Citizen citizen, BigDecimal readingKwh, LocalDateTime at) {
        if (citizen.getLastReadingAt() != null && readingKwh.compareTo(citizen.getLastReadingKwh()) <= 0) {
            throw new InvalidReadingException("Reading " + readingKwh + " kWh must be more than the last reading of "
                    + citizen.getLastReadingKwh() + " kWh");
        }
        client.send(properties.providerServiceId(citizen.getProviderCode()),
                new ReadingPayload(citizen.getId(), citizen.getMeterType(), readingKwh, at));
        citizen.accept(readingKwh, at);
        return repository.save(citizen);
    }
}
