package com.sdem.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Front end settings.
 *
 * @param version    version of the web application, shown in the footer
 * @param apiVersion version of the microservice APIs the front end calls
 * @param gatewayUrl API gateway address; every REST call goes through it
 * @param direct     optional service id to URL overrides (integration tests only)
 * @param months     default number of months shown
 * @param cities     the city modules shown in the UI
 * @param providers  the electricity providers
 * @param users      users created at start up in the in-memory user store
 */
@ConfigurationProperties(prefix = "sdem.frontend")
public record FrontEndProperties(
        @DefaultValue("1.0.0") String version,
        @DefaultValue("v1") String apiVersion,
        @DefaultValue("http://localhost:8080") String gatewayUrl,
        Map<String, String> direct,
        @DefaultValue("12") int months,
        List<City> cities,
        List<Provider> providers,
        List<UserSeed> users) {

    /** A city module. */
    public record City(String id, String name) {

        /** Service id of the city's Smart City microservice, for example smartcity-newcastle. */
        public String smartCityServiceId() {
            return "smartcity-" + id;
        }
    }

    /** An electricity provider. */
    public record Provider(String code, String name) {
    }

    /** A user created at start up. */
    public record UserSeed(String username, String password, String role) {
    }

    public Optional<City> city(String id) {
        return cities.stream().filter(c -> c.id().equalsIgnoreCase(id)).findFirst();
    }

    public Optional<Provider> provider(String code) {
        return providers.stream().filter(p -> p.code().equalsIgnoreCase(code)).findFirst();
    }

    /** Base URL for a microservice, through the gateway unless overridden. */
    public String baseUrl(String serviceId) {
        if (direct != null && direct.containsKey(serviceId)) {
            return direct.get(serviceId);
        }
        return gatewayUrl + "/" + serviceId;
    }
}
