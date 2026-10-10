package com.sdem.city.repository;

import com.sdem.city.entities.CitySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CitySnapshotRepository extends JpaRepository<CitySnapshot, Long> {

    List<CitySnapshot> findAllByOrderByTakenAtDesc();
}
