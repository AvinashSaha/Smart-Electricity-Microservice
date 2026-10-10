package com.sdem.citizen.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;

/**
 * How this service reaches other microservices.
 *
 * <p>By default every call goes through the API gateway as
 * {@code {gatewayUrl}/{serviceId}/...}. Entries in {@code direct} bypass the
 * gateway for a given service id; they are only used by the integration tests.</p>
 */
@ConfigurationProperties(prefix = "sdem.routing")
public record RoutingProperties(
        @DefaultValue("http://localhost:8080") String gatewayUrl,
        Map<String, String> direct) {

    /** Base URL for a service id. */
    public String baseUrl(String serviceId) {
        if (direct != null && direct.containsKey(serviceId)) {
            return direct.get(serviceId);
        }
        return gatewayUrl + "/" + serviceId;
    }
}
