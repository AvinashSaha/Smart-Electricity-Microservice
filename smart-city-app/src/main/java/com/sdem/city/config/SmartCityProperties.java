package com.sdem.city.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;
import java.util.Locale;

/**
 * Settings for this city instance.
 *
 * @param city      city id, for example {@code newcastle}
 * @param providers provider codes in this city
 * @param months    how many months of history to collect on each run
 */
@ConfigurationProperties(prefix = "sdem.city")
public record SmartCityProperties(
        String city,
        @DefaultValue({"a", "b", "c"}) List<String> providers,
        @DefaultValue("12") int months) {

    /** Service id of a provider instance, for example provider-newcastle-a. */
    public String providerServiceId(String code) {
        return "provider-" + city + "-" + code.toLowerCase(Locale.UK);
    }

    public String displayCity() {
        return city.substring(0, 1).toUpperCase(Locale.UK) + city.substring(1);
    }
}
