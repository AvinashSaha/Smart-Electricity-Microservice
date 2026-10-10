package com.sdem.provider.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A cumulative meter reading received from a citizen or smart meter.
 * {@code consumptionKwh} is the energy used since that citizen's previous reading.
 */
@Entity
@Table(name = "meter_readings", indexes = {
        @Index(name = "idx_reading_citizen_time", columnList = "citizenId, recordedAt"),
        @Index(name = "idx_reading_time", columnList = "recordedAt")
})
public class MeterReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String citizenId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MeterType meterType;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal readingKwh;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal consumptionKwh;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    protected MeterReading() {
        // required by JPA
    }

    public MeterReading(String citizenId, MeterType meterType, BigDecimal readingKwh,
                        BigDecimal consumptionKwh, LocalDateTime recordedAt, LocalDateTime receivedAt) {
        this.citizenId = citizenId;
        this.meterType = meterType;
        this.readingKwh = readingKwh;
        this.consumptionKwh = consumptionKwh;
        this.recordedAt = recordedAt;
        this.receivedAt = receivedAt;
    }

    public Long getId() { return id; }
    public String getCitizenId() { return citizenId; }
    public MeterType getMeterType() { return meterType; }
    public BigDecimal getReadingKwh() { return readingKwh; }
    public BigDecimal getConsumptionKwh() { return consumptionKwh; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
}
