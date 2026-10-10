package com.sdem.provider.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;

/**
 * Identity of this provider instance.
 *
 * @param city city id, for example {@code newcastle}
 * @param code provider code, for example {@code a}
 */
@ConfigurationProperties(prefix = "sdem.provider")
public record ProviderProperties(String city, String code) {

    /** Provider code in display form, for example "A". */
    public String displayCode() {
        return code.toUpperCase(Locale.UK);
    }

    /** City name in display form, for example "Newcastle". */
    public String displayCity() {
        return city.substring(0, 1).toUpperCase(Locale.UK) + city.substring(1);
    }
}
