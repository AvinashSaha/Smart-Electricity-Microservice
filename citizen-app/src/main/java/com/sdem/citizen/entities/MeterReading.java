package com.sdem.citizen.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A cumulative smart meter reading (the number shown on the meter). */
@Entity
@Table(name = "meter_readings")
public class MeterReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citizen_id")
    private Citizen citizen;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal readingKwh;

    @Column(nullable = false)
    private LocalDateTime readingAt;

    public MeterReading() {
    }

    public MeterReading(BigDecimal readingKwh, LocalDateTime readingAt) {
        this.readingKwh = readingKwh;
        this.readingAt = readingAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Citizen getCitizen() { return citizen; }
    public void setCitizen(Citizen citizen) { this.citizen = citizen; }

    public BigDecimal getReadingKwh() { return readingKwh; }
    public void setReadingKwh(BigDecimal readingKwh) { this.readingKwh = readingKwh; }

    public LocalDateTime getReadingAt() { return readingAt; }
    public void setReadingAt(LocalDateTime readingAt) { this.readingAt = readingAt; }
}
