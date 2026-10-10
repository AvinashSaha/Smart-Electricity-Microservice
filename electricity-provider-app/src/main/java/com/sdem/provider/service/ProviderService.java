package com.sdem.provider.service;

import com.sdem.provider.bean.BillQuote;
import com.sdem.provider.bean.ProviderRequest;
import com.sdem.provider.entities.Provider;
import com.sdem.provider.exception.DuplicateProviderException;
import com.sdem.provider.exception.ProviderNotFoundException;
import com.sdem.provider.repository.ProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class ProviderService {

    /** Used to convert pence to pounds and percentages to fractions. */
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final ProviderRepository repository;

    public ProviderService(ProviderRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Provider> findAllActive() {
        return repository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Provider findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProviderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Provider findCheapest() {
        return repository.findFirstByActiveTrueOrderByUnitRatePenceAsc()
                .orElseThrow(() -> new IllegalArgumentException("No active providers are available"));
    }

    public Provider create(ProviderRequest request) {
        if (repository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateProviderException(request.name());
        }
        Provider provider = new Provider(request.name(), request.unitRatePence(),
                request.standingChargePence(), request.renewablePercent());
        return repository.save(provider);
    }

    public Provider update(Long id, ProviderRequest request) {
        Provider provider = findById(id);
        provider.setName(request.name());
        provider.setUnitRatePence(request.unitRatePence());
        provider.setStandingChargePence(request.standingChargePence());
        provider.setRenewablePercent(request.renewablePercent());
        return repository.save(provider);
    }

    /** Soft delete so historic bills and reports still resolve the provider. */
    public void deactivate(Long id) {
        Provider provider = findById(id);
        provider.setActive(false);
        repository.save(provider);
    }

    @Transactional(readOnly = true)
    public BillQuote quote(Long id, BigDecimal kwh, int days) {
        return calculate(findById(id), kwh, days);
    }

    /**
     * Bill = (kWh x unit rate) + (days x standing charge), converted from pence to pounds.
     */
    public static BillQuote calculate(Provider provider, BigDecimal kwh, int days) {
        if (kwh == null || kwh.signum() < 0) {
            throw new IllegalArgumentException("kwh must be zero or more");
        }
        if (days < 0) {
            throw new IllegalArgumentException("days must be zero or more");
        }
        BigDecimal energy = kwh.multiply(provider.getUnitRatePence())
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal standing = provider.getStandingChargePence().multiply(BigDecimal.valueOf(days))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal renewable = kwh.multiply(BigDecimal.valueOf(provider.getRenewablePercent()))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        return new BillQuote(provider.getId(), provider.getName(), kwh, days,
                energy, standing, energy.add(standing), renewable);
    }
}
