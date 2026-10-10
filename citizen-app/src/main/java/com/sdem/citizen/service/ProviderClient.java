package com.sdem.citizen.service;

import com.sdem.citizen.bean.ProviderInfo;
import com.sdem.citizen.bean.ProviderQuote;
import com.sdem.citizen.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

/**
 * Talks to the Electricity Provider microservice through Eureka service discovery.
 */
@Component
public class ProviderClient {

    private final RestClient restClient;

    public ProviderClient(RestClient.Builder loadBalancedBuilder,
                          @Value("${sdem.provider-service.url:http://electricity-provider-app}") String baseUrl) {
        this.restClient = loadBalancedBuilder.clone().baseUrl(baseUrl).build();
    }

    public ProviderInfo getProvider(Long providerId) {
        try {
            return restClient.get()
                    .uri("/api/providers/{id}", providerId)
                    .retrieve()
                    .body(ProviderInfo.class);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new IllegalArgumentException("Provider " + providerId + " does not exist");
            }
            throw ex;
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Electricity Provider service is unavailable", ex);
        }
    }

    public ProviderQuote quote(Long providerId, BigDecimal kwh, int days) {
        try {
            return restClient.get()
                    .uri(uri -> uri.path("/api/providers/{id}/quote")
                            .queryParam("kwh", kwh)
                            .queryParam("days", days)
                            .build(providerId))
                    .retrieve()
                    .body(ProviderQuote.class);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new IllegalArgumentException("Provider " + providerId + " does not exist");
            }
            throw ex;
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Electricity Provider service is unavailable", ex);
        }
    }
}
