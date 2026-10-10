package com.sdem.web.bean;

import java.math.BigDecimal;

/** Form backing object for adding a provider. */
public class ProviderForm {

    private String name;
    private BigDecimal unitRatePence;
    private BigDecimal standingChargePence;
    private int renewablePercent;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getUnitRatePence() { return unitRatePence; }
    public void setUnitRatePence(BigDecimal unitRatePence) { this.unitRatePence = unitRatePence; }

    public BigDecimal getStandingChargePence() { return standingChargePence; }
    public void setStandingChargePence(BigDecimal standingChargePence) { this.standingChargePence = standingChargePence; }

    public int getRenewablePercent() { return renewablePercent; }
    public void setRenewablePercent(int renewablePercent) { this.renewablePercent = renewablePercent; }
}
