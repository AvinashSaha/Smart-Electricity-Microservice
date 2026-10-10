package com.sdem.main;

import java.util.List;
import java.util.Locale;

/**
 * Ports and simulation settings for one city.
 *
 * <p>Each city gets a block of ten ports: base + 0 is the Smart City service,
 * base + 1 to 3 are providers A to C, base + 4 is the Citizen service.
 * Newcastle uses 8110 to 8114, Sunderland 8120 to 8124, and so on.</p>
 *
 * @param id                city id used in service names, for example "newcastle"
 * @param basePort          first port of the city's block
 * @param consumptionFactor multiplier so each city's consumption differs
 */
public record CityPlan(String id, int basePort, double consumptionFactor) {

    public static final List<String> PROVIDERS = List.of("a", "b", "c");

    /** The five pilot cities of the North East region. */
    public static final List<CityPlan> NORTH_EAST = List.of(
            new CityPlan("newcastle", 8110, 1.10),
            new CityPlan("sunderland", 8120, 0.95),
            new CityPlan("durham", 8130, 0.90),
            new CityPlan("middlesbrough", 8140, 1.05),
            new CityPlan("darlington", 8150, 0.85));

    public int smartCityPort() {
        return basePort;
    }

    public int providerPort(String code) {
        return basePort + 1 + PROVIDERS.indexOf(code.toLowerCase(Locale.UK));
    }

    public int citizenPort() {
        return basePort + 4;
    }

    /** Finds a city by id (case insensitive). */
    public static CityPlan byId(String id) {
        return NORTH_EAST.stream()
                .filter(c -> c.id().equalsIgnoreCase(id.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown city '" + id + "'. Known cities: "
                        + NORTH_EAST.stream().map(CityPlan::id).toList()));
    }
}
