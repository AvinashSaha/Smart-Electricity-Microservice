package com.sdem.citizen.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Simulation settings for one city.
 *
 * @param city                   city id, for example {@code newcastle}
 * @param providers              provider codes in the city
 * @param manualPerProvider      citizens entering readings by hand, per provider
 * @param smartPerProvider       smart meters per provider
 * @param smartIncrementMin      smallest increase a smart meter reports per tick (kWh)
 * @param smartIncrementMax      largest increase a smart meter reports per tick (kWh)
 * @param manualIncrementMin     smallest increase a citizen enters (kWh)
 * @param manualIncrementMax     largest increase a citizen enters (kWh)
 * @param manualSubmitChance     chance (0 to 1) that a given citizen submits on a tick
 * @param backfillMonths         months of history each citizen sends on first run
 * @param monthlyMinKwh          lowest monthly consumption used for history (kWh)
 * @param monthlyMaxKwh          highest monthly consumption used for history (kWh)
 * @param consumptionFactor      multiplier for this city (lets cities differ)
 * @param providerFactors        multiplier per provider code
 */
@ConfigurationProperties(prefix = "sdem.simulation")
public record SimulationProperties(
        String city,
        @DefaultValue({"a", "b", "c"}) List<String> providers,
        @DefaultValue("100") int manualPerProvider,
        @DefaultValue("50") int smartPerProvider,
        @DefaultValue("0.002") BigDecimal smartIncrementMin,
        @DefaultValue("0.020") BigDecimal smartIncrementMax,
        @DefaultValue("0.5") BigDecimal manualIncrementMin,
        @DefaultValue("5.0") BigDecimal manualIncrementMax,
        @DefaultValue("0.05") double manualSubmitChance,
        @DefaultValue("12") int backfillMonths,
        @DefaultValue("180") BigDecimal monthlyMinKwh,
        @DefaultValue("320") BigDecimal monthlyMaxKwh,
        @DefaultValue("1.0") double consumptionFactor,
        Map<String, Double> providerFactors) {

    /** Service id of a provider instance in this city, for example provider-newcastle-a. */
    public String providerServiceId(String code) {
        return "provider-" + city + "-" + code.toLowerCase(Locale.UK);
    }

    public double providerFactor(String code) {
        if (providerFactors == null) {
            return 1.0;
        }
        return providerFactors.getOrDefault(code.toLowerCase(Locale.UK), 1.0);
    }
}
