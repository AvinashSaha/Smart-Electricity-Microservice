package com.sdem.city.service;

import com.sdem.city.bean.CitizenUsage;
import com.sdem.city.bean.ProviderInfo;
import com.sdem.city.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Calls the Citizen and Electricity Provider services by their Eureka names.
 */
@Component
public class DownstreamClient {

    private final RestClient citizenClient;
    private final RestClient providerClient;

    public DownstreamClient(RestClient.Builder loadBalancedBuilder,
                            @Value("${sdem.citizen-service.url:http://citizen-app}") String citizenUrl,
                            @Value("${sdem.provider-service.url:http://electricity-provider-app}") String providerUrl) {
        this.citizenClient = loadBalancedBuilder.clone().baseUrl(citizenUrl).build();
        this.providerClient = loadBalancedBuilder.clone().baseUrl(providerUrl).build();
    }

    public List<CitizenUsage> citizenUsage() {
        try {
            List<CitizenUsage> usage = citizenClient.get()
                    .uri("/api/citizens/consumption")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CitizenUsage>>() { });
            return usage == null ? List.of() : usage;
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Citizen service is unavailable", ex);
        }
    }

    public List<ProviderInfo> providers() {
        try {
            List<ProviderInfo> providers = providerClient.get()
                    .uri("/api/providers")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ProviderInfo>>() { });
            return providers == null ? List.of() : providers;
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Electricity Provider service is unavailable", ex);
        }
    }
}
