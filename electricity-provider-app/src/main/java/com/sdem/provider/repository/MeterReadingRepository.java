package com.sdem.provider.repository;

import com.sdem.provider.bean.ReadingPoint;
import com.sdem.provider.entities.MeterReading;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

    Optional<MeterReading> findTopByCitizenIdOrderByRecordedAtDesc(String citizenId);

    List<MeterReading> findByCitizenIdOrderByRecordedAtDesc(String citizenId, Pageable page);

    List<MeterReading> findAllByOrderByRecordedAtDesc(Pageable page);

    /** Lightweight rows used to build monthly totals without loading whole entities. */
    @Query("select new com.sdem.provider.bean.ReadingPoint(r.citizenId, r.consumptionKwh, r.recordedAt) "
            + "from MeterReading r where r.recordedAt >= :from")
    List<ReadingPoint> findPointsSince(@Param("from") LocalDateTime from);

    @Query("select count(distinct r.citizenId) from MeterReading r")
    long countCitizens();

    @Query("select coalesce(sum(r.consumptionKwh), 0) from MeterReading r")
    java.math.BigDecimal totalConsumption();

    @Query("select max(r.recordedAt) from MeterReading r")
    LocalDateTime latestRecordedAt();
}
