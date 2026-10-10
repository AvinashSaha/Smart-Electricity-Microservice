package com.sdem.provider.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "providers")
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** Price per kWh in pence. */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal unitRatePence;

    /** Fixed daily standing charge in pence. */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal standingChargePence;

    /** Share of supply that comes from renewable sources, 0 to 100. */
    @Column(nullable = false)
    private int renewablePercent;

    @Column(nullable = false)
    private boolean active = true;

    public Provider() {
    }

    public Provider(String name, BigDecimal unitRatePence, BigDecimal standingChargePence, int renewablePercent) {
        this.name = name;
        this.unitRatePence = unitRatePence;
        this.standingChargePence = standingChargePence;
        this.renewablePercent = renewablePercent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getUnitRatePence() { return unitRatePence; }
    public void setUnitRatePence(BigDecimal unitRatePence) { this.unitRatePence = unitRatePence; }

    public BigDecimal getStandingChargePence() { return standingChargePence; }
    public void setStandingChargePence(BigDecimal standingChargePence) { this.standingChargePence = standingChargePence; }

    public int getRenewablePercent() { return renewablePercent; }
    public void setRenewablePercent(int renewablePercent) { this.renewablePercent = renewablePercent; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
