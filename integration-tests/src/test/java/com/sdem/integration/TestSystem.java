package com.sdem.integration;

import com.sdem.citizen.CitizenApplication;
import com.sdem.city.SmartCityApplication;
import com.sdem.provider.ProviderApplication;
import com.sdem.web.FrontEndApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Starts a small but complete SDEM system inside the test JVM: three
 * Electricity Provider instances, a Smart City instance and a Citizen
 * instance for Newcastle, plus the web front end. Each is a real Spring Boot
 * application listening on its own port and talking to the others over HTTP.
 *
 * <p>Eureka and the gateway are replaced by direct URLs (the services'
 * {@code direct} routing option) so the tests do not need extra processes.
 * Databases are in memory so every run starts clean.</p>
 */
final class TestSystem implements AutoCloseable {

    static final String CITY = "newcastle";
    static final List<String> PROVIDERS = List.of("a", "b", "c");

    /** Spring Security is on the shared test classpath (front end), so switch it off for the REST services. */
    private static final String NO_SECURITY = "--spring.autoconfigure.exclude="
            + "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,"
            + "org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration";

    final Map<String, Integer> providerPorts = new LinkedHashMap<>();
    final int smartCityPort;
    final int citizenPort;
    final int frontEndPort;

    final Map<String, ConfigurableApplicationContext> providers = new LinkedHashMap<>();
    ConfigurableApplicationContext smartCity;
    ConfigurableApplicationContext citizen;
    ConfigurableApplicationContext frontEnd;

    TestSystem() {
        PROVIDERS.forEach(p -> providerPorts.put(p, freePort()));
        smartCityPort = freePort();
        citizenPort = freePort();
        frontEndPort = freePort();
    }

    TestSystem start() {
        for (String code : PROVIDERS) {
            providers.put(code, run(ProviderApplication.create(), providerPorts.get(code),
                    "--sdem.provider.city=" + CITY, "--sdem.provider.code=" + code,
                    "--spring.datasource.url=jdbc:h2:mem:provider-" + code + ";DB_CLOSE_DELAY=-1", NO_SECURITY));
        }
        List<String> cityArgs = new ArrayList<>(List.of("--sdem.city.city=" + CITY,
                "--sdem.city.months=3",
                "--sdem.city.collect-initial-delay=PT1H",
                "--spring.datasource.url=jdbc:h2:mem:smartcity;DB_CLOSE_DELAY=-1", NO_SECURITY));
        cityArgs.addAll(directProviderRoutes());
        smartCity = run(SmartCityApplication.create(), smartCityPort, cityArgs.toArray(String[]::new));

        List<String> citizenArgs = new ArrayList<>(List.of("--sdem.simulation.city=" + CITY,
                "--sdem.simulation.manual-per-provider=2",
                "--sdem.simulation.smart-per-provider=1",
                "--sdem.simulation.backfill-months=3",
                "--sdem.simulation.initial-delay=PT1H",
                "--spring.datasource.url=jdbc:h2:mem:citizen;DB_CLOSE_DELAY=-1", NO_SECURITY));
        citizenArgs.addAll(directProviderRoutes());
        citizen = run(CitizenApplication.create(), citizenPort, citizenArgs.toArray(String[]::new));

        frontEnd = run(FrontEndApplication.create(), frontEndPort,
                // only Newcastle is running; every other city goes to an unreachable gateway
                "--sdem.frontend.gateway-url=http://localhost:1",
                "--sdem.frontend.direct.smartcity-" + CITY + "=" + url(smartCityPort),
                "--spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration");
        return this;
    }

    private List<String> directProviderRoutes() {
        List<String> args = new ArrayList<>();
        providerPorts.forEach((code, port) ->
                args.add("--sdem.routing.direct.provider-" + CITY + "-" + code + "=" + url(port)));
        return args;
    }

    private static ConfigurableApplicationContext run(SpringApplication app, int port, String... extra) {
        List<String> args = new ArrayList<>(List.of("--server.port=" + port, "--eureka.client.enabled=false",
                "--spring.main.banner-mode=off"));
        args.addAll(List.of(extra));
        return app.run(args.toArray(String[]::new));
    }

    static String url(int port) {
        return "http://localhost:" + port;
    }

    <T> T bean(ConfigurableApplicationContext context, Class<T> type) {
        return context.getBean(type);
    }

    private static int freePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException ex) {
            throw new IllegalStateException("No free port", ex);
        }
    }

    @Override
    public void close() {
        for (ConfigurableApplicationContext c : new ConfigurableApplicationContext[]{frontEnd, citizen, smartCity}) {
            if (c != null) {
                c.close();
            }
        }
        providers.values().forEach(ConfigurableApplicationContext::close);
    }
}
