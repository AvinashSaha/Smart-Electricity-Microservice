package com.sdem.citizen.repository;

import com.sdem.citizen.entities.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CitizenRepository extends JpaRepository<Citizen, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByMeterId(String meterId);

    List<Citizen> findByProviderId(Long providerId);
}
