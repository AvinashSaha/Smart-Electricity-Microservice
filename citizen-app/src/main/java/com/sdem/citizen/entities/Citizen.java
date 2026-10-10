package com.sdem.citizen.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A citizen (manual entry) or smart meter contracted with one provider. Keeps
 * the last reading that was accepted by the provider so the next one can be
 * checked to be higher.
 */
@Entity
@Table(name = "citizens")
public class Citizen {

    /** For example NEWCASTLE-A-M007 (manual) or NEWCASTLE-A-S012 (smart meter). */
    @Id
    @Column(length = 40)
    private String id;

    @Column(nullable = false, length = 5)
    private String providerCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MeterType meterType;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal lastReadingKwh;

    /** Null until the first reading has been accepted by the provider. */
    private LocalDateTime lastReadingAt;

    /** Whether the history readings have been sent. */
    @Column(nullable = false)
    private boolean backfilled;

    protected Citizen() {
        // required by JPA
    }

    public Citizen(String id, String providerCode, MeterType meterType, BigDecimal openingReadingKwh) {
        this.id = id;
        this.providerCode = providerCode;
        this.meterType = meterType;
        this.lastReadingKwh = openingReadingKwh;
    }

    /** Records a reading that the provider has accepted. */
    public void accept(BigDecimal readingKwh, LocalDateTime at) {
        this.lastReadingKwh = readingKwh;
        this.lastReadingAt = at;
    }

    public void markBackfilled() {
        this.backfilled = true;
    }

    public String getId() { return id; }
    public String getProviderCode() { return providerCode; }
    public MeterType getMeterType() { return meterType; }
    public BigDecimal getLastReadingKwh() { return lastReadingKwh; }
    public LocalDateTime getLastReadingAt() { return lastReadingAt; }
    public boolean isBackfilled() { return backfilled; }
}
