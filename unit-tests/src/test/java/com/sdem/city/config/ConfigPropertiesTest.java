package com.sdem.city.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigPropertiesTest {

    @Test
    void buildsProviderServiceIds() {
        SmartCityProperties props = new SmartCityProperties("durham", List.of("a", "b", "c"), 12);
        assertThat(props.providerServiceId("B")).isEqualTo("provider-durham-b");
        assertThat(props.displayCity()).isEqualTo("Durham");
    }

    @Test
    void routesThroughTheGatewayUnlessOverridden() {
        RoutingProperties routing = new RoutingProperties("http://localhost:8080",
                Map.of("provider-durham-c", "http://localhost:9999"));
        assertThat(routing.baseUrl("provider-durham-a")).isEqualTo("http://localhost:8080/provider-durham-a");
        assertThat(routing.baseUrl("provider-durham-c")).isEqualTo("http://localhost:9999");
    }
}
