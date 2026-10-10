package com.sdem.citizen.client;

import com.sdem.citizen.bean.ReadingPayload;
import com.sdem.citizen.config.RoutingProperties;
import com.sdem.citizen.exception.InvalidReadingException;
import com.sdem.citizen.exception.ProviderUnavailableException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Sends readings to Electricity Provider instances through the API gateway. */
@Component
public class ProviderClient {

    private final RestClient restClient;
    private final RoutingProperties routing;

    public ProviderClient(RestClient.Builder builder, RoutingProperties routing) {
        this.restClient = builder.build();
        this.routing = routing;
    }

    /**
     * Posts one reading in real time.
     *
     * @throws InvalidReadingException      if the provider rejects the reading (4xx)
     * @throws ProviderUnavailableException if the provider or gateway cannot be reached
     */
    public void send(String providerServiceId, ReadingPayload payload) {
        try {
            restClient.post()
                    .uri(routing.baseUrl(providerServiceId) + "/api/v1/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException ex) {
            throw new InvalidReadingException(providerServiceId + " rejected the reading: "
                    + ex.getResponseBodyAsString());
        } catch (RestClientException ex) {
            throw new ProviderUnavailableException(providerServiceId + " could not be reached", ex);
        }
    }
}
