package com.sdem.citizen.service;

import com.sdem.citizen.entities.Citizen;
import com.sdem.citizen.entities.MeterType;
import com.sdem.citizen.exception.CitizenNotFoundException;
import com.sdem.citizen.exception.InvalidReadingException;
import com.sdem.citizen.repository.CitizenRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

/** Manual entry by citizens, and lookups. */
@Service
public class CitizenService {

    private final CitizenRepository repository;
    private final ReadingSender sender;
    private final Clock clock;

    public CitizenService(CitizenRepository repository, ReadingSender sender, Clock clock) {
        this.repository = repository;
        this.sender = sender;
        this.clock = clock;
    }

    public List<Citizen> list(String providerCode) {
        return providerCode == null || providerCode.isBlank()
                ? repository.findAll()
                : repository.findByProviderCodeOrderById(providerCode.toUpperCase(Locale.UK));
    }

    public Citizen get(String id) {
        return repository.findById(id).orElseThrow(() -> new CitizenNotFoundException(id));
    }

    /**
     * A citizen types in a reading. It is sent straight away (real time) to
     * their provider. Smart meters cannot be read manually.
     */
    public Citizen submitManual(String id, BigDecimal readingKwh) {
        Citizen citizen = get(id);
        if (citizen.getMeterType() != MeterType.MANUAL) {
            throw new InvalidReadingException(id + " is a smart meter; its readings are sent automatically");
        }
        return sender.send(citizen, readingKwh, LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS));
    }
}
