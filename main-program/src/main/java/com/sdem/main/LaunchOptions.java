package com.sdem.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command line options for the main program.
 *
 * @param cities      cities to run
 * @param dataDir     folder for the database files
 * @param gatewayUrl  API gateway address
 * @param eurekaUrl   service registry address
 * @param passThrough any other --key=value arguments, passed to every microservice
 */
public record LaunchOptions(List<CityPlan> cities, String dataDir, String gatewayUrl, String eurekaUrl,
                            List<String> passThrough) {

    public static LaunchOptions parse(String... args) {
        List<CityPlan> cities = CityPlan.NORTH_EAST;
        String dataDir = env("SDEM_DATA_DIR", "./data");
        String gateway = env("SDEM_GATEWAY_URL", "http://localhost:8080");
        String eureka = env("EUREKA_URL", "http://localhost:8761/eureka");
        List<String> passThrough = new ArrayList<>();
        for (String arg : args) {
            if (arg.startsWith("--cities=")) {
                cities = Arrays.stream(value(arg).split(",")).map(CityPlan::byId).toList();
            } else if (arg.startsWith("--data-dir=")) {
                dataDir = value(arg);
            } else if (arg.startsWith("--gateway-url=")) {
                gateway = value(arg);
            } else if (arg.startsWith("--eureka-url=")) {
                eureka = value(arg);
            } else if (arg.startsWith("--")) {
                passThrough.add(arg);
            } else {
                throw new IllegalArgumentException("Unrecognised argument: " + arg);
            }
        }
        if (cities.isEmpty()) {
            throw new IllegalArgumentException("At least one city is required");
        }
        return new LaunchOptions(cities, dataDir, gateway, eureka, passThrough);
    }

    private static String value(String arg) {
        return arg.substring(arg.indexOf('=') + 1);
    }

    private static String env(String name, String fallback) {
        String v = System.getenv(name);
        return v == null || v.isBlank() ? fallback : v;
    }
}
