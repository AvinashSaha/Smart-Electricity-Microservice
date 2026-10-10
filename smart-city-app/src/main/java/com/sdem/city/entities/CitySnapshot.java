package com.sdem.city.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A saved copy of the headline city figures, so trends can be tracked over time. */
@Entity
@Table(name = "city_snapshots")
public class CitySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime takenAt;

    @Column(nullable = false)
    private long citizenCount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalKwh;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal renewableKwh;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal co2AvoidedKg;

    public CitySnapshot() {
    }

    public CitySnapshot(LocalDateTime takenAt, long citizenCount, BigDecimal totalKwh,
                        BigDecimal renewableKwh, BigDecimal co2AvoidedKg) {
        this.takenAt = takenAt;
        this.citizenCount = citizenCount;
        this.totalKwh = totalKwh;
        this.renewableKwh = renewableKwh;
        this.co2AvoidedKg = co2AvoidedKg;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTakenAt() { return takenAt; }
    public void setTakenAt(LocalDateTime takenAt) { this.takenAt = takenAt; }

    public long getCitizenCount() { return citizenCount; }
    public void setCitizenCount(long citizenCount) { this.citizenCount = citizenCount; }

    public BigDecimal getTotalKwh() { return totalKwh; }
    public void setTotalKwh(BigDecimal totalKwh) { this.totalKwh = totalKwh; }

    public BigDecimal getRenewableKwh() { return renewableKwh; }
    public void setRenewableKwh(BigDecimal renewableKwh) { this.renewableKwh = renewableKwh; }

    public BigDecimal getCo2AvoidedKg() { return co2AvoidedKg; }
    public void setCo2AvoidedKg(BigDecimal co2AvoidedKg) { this.co2AvoidedKg = co2AvoidedKg; }
}
