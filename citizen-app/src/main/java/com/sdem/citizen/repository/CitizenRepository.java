package com.sdem.citizen.repository;

import com.sdem.citizen.entities.Citizen;
import com.sdem.citizen.entities.MeterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CitizenRepository extends JpaRepository<Citizen, String> {

    List<Citizen> findByMeterTypeOrderById(MeterType meterType);

    List<Citizen> findByProviderCodeOrderById(String providerCode);

    List<Citizen> findByBackfilledFalseOrderById();

    long countByMeterType(MeterType meterType);

    long countByBackfilledFalse();
}
