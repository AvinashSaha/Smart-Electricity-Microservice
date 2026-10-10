package com.sdem.city.repository;

import com.sdem.city.entities.AggregatedConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AggregatedConsumptionRepository extends JpaRepository<AggregatedConsumption, Long> {

    Optional<AggregatedConsumption> findByProviderAndMonth(String provider, String month);

    List<AggregatedConsumption> findByMonthInOrderByMonthAscProviderAsc(Collection<String> months);
}
