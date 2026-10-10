package com.sdem.provider.repository;

import com.sdem.provider.entities.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    List<Provider> findByActiveTrueOrderByNameAsc();

    Optional<Provider> findFirstByActiveTrueOrderByUnitRatePenceAsc();

    boolean existsByNameIgnoreCase(String name);
}
