package com.sdem.main;

import com.sdem.citizen.CitizenApplication;
import com.sdem.city.SmartCityApplication;
import com.sdem.provider.ProviderApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Main program: simulates the Smart City system.
 *
 * <p>For every selected city it runs one Smart City microservice, three
 * Electricity Provider microservices (A, B and C) and one Citizen
 * microservice that simulates 100 citizens for manual entry and 50 smart
 * meters for automated entry per provider. Each microservice is a separate
 * Spring Boot application with its own port, its own database file and its
 * own Eureka registration; they only talk to each other through the API
 * gateway.</p>
 *
 * <p>The service registry and the API gateway must be running first.</p>
 *
 * <pre>
 * java -jar main-program.jar                        all five cities
 * java -jar main-program.jar --cities=newcastle     one city
 * java -jar main-program.jar --cities=newcastle,durham --data-dir=./data
 * </pre>
 */
public final class MainProgram {

    private static final Logger LOG = LoggerFactory.getLogger(MainProgram.class);

    private final List<ConfigurableApplicationContext> running = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws Exception {
        LaunchOptions options = LaunchOptions.parse(args);
        new MainProgram().start(options);
    }

    /** Starts every selected city in parallel and registers a shutdown hook to stop them. */
    public void start(LaunchOptions options) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "sdem-shutdown"));
        ExecutorService pool = Executors.newFixedThreadPool(options.cities().size());
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (CityPlan city : options.cities()) {
                futures.add(pool.submit(() -> startCity(city, options)));
            }
            for (Future<?> f : futures) {
                f.get();
            }
        } finally {
            pool.shutdown();
        }
        LOG.info("Smart City system running for {} with {} microservice instances",
                options.cities().stream().map(CityPlan::id).toList(), running.size());
    }

    /** Starts the providers first, then the smart city service, then the citizen simulation. */
    void startCity(CityPlan city, LaunchOptions options) {
        for (String code : CityPlan.PROVIDERS) {
            running.add(ProviderApplication.create().run(common(options, city.providerPort(code),
                    "--sdem.provider.city=" + city.id(),
                    "--sdem.provider.code=" + code)));
        }
        running.add(SmartCityApplication.create().run(common(options, city.smartCityPort(),
                "--sdem.city.city=" + city.id(),
                "--sdem.routing.gateway-url=" + options.gatewayUrl())));
        running.add(CitizenApplication.create().run(common(options, city.citizenPort(),
                "--sdem.simulation.city=" + city.id(),
                "--sdem.simulation.consumption-factor=" + String.format(Locale.UK, "%.2f", city.consumptionFactor()),
                "--sdem.routing.gateway-url=" + options.gatewayUrl())));
        LOG.info("{}: smart city on {}, providers on {}-{}, citizens on {}", city.id(), city.smartCityPort(),
                city.providerPort("a"), city.providerPort("c"), city.citizenPort());
    }

    private static String[] common(LaunchOptions options, int port, String... extra) {
        List<String> args = new ArrayList<>(List.of(
                "--server.port=" + port,
                "--sdem.data-dir=" + options.dataDir(),
                "--eureka.client.service-url.defaultZone=" + options.eurekaUrl()));
        args.addAll(Arrays.asList(extra));
        args.addAll(options.passThrough());
        return args.toArray(String[]::new);
    }

    /** Closes every running microservice. */
    public void stop() {
        for (ConfigurableApplicationContext context : running) {
            try {
                context.close();
            } catch (RuntimeException ex) {
                LOG.warn("Error while stopping {}: {}", context.getId(), ex.getMessage());
            }
        }
        running.clear();
    }
}
