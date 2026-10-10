package com.sdem.city.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Aggregated monthly totals for one provider, stored permanently. Only totals
 * are stored; averages are always calculated when needed.
 */
@Entity
@Table(name = "aggregated_consumption",
        uniqueConstraints = @UniqueConstraint(name = "uk_provider_month", columnNames = {"provider", "period_month"}))
public class AggregatedConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5)
    private String provider;

    /** ISO month, for example 2026-03. */
    @Column(name = "period_month", nullable = false, length = 7)
    private String month;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal totalKwh;

    @Column(nullable = false)
    private long readingCount;

    @Column(nullable = false)
    private long citizenCount;

    @Column(nullable = false)
    private LocalDateTime collectedAt;

    protected AggregatedConsumption() {
        // required by JPA
    }

    public AggregatedConsumption(String provider, String month) {
        this.provider = provider;
        this.month = month;
    }

    /** Replaces the totals with the latest figures from the provider. */
    public void update(BigDecimal totalKwh, long readingCount, long citizenCount, LocalDateTime collectedAt) {
        this.totalKwh = totalKwh;
        this.readingCount = readingCount;
        this.citizenCount = citizenCount;
        this.collectedAt = collectedAt;
    }

    public Long getId() { return id; }
    public String getProvider() { return provider; }
    public String getMonth() { return month; }
    public BigDecimal getTotalKwh() { return totalKwh; }
    public long getReadingCount() { return readingCount; }
    public long getCitizenCount() { return citizenCount; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
}
