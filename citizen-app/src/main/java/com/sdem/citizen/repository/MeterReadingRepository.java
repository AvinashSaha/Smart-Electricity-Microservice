package com.sdem.citizen.repository;

import com.sdem.citizen.entities.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

    List<MeterReading> findByCitizenIdOrderByReadingAtAsc(Long citizenId);

    Optional<MeterReading> findFirstByCitizenIdOrderByReadingAtDesc(Long citizenId);
}
