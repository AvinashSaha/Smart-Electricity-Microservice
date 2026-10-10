package com.sdem.city.client;

import com.sdem.city.bean.ProviderMonthly;
import com.sdem.city.config.RoutingProperties;
import com.sdem.city.exception.ProviderUnavailableException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/** Requests data from Electricity Provider instances through the API gateway. */
@Component
public class ProviderClient {

    private final RestClient restClient;
    private final RoutingProperties routing;

    public ProviderClient(RestClient.Builder builder, RoutingProperties routing) {
        this.restClient = builder.build();
        this.routing = routing;
    }

    /**
     * @param serviceId provider service id, for example provider-newcastle-a
     * @param months    number of months to request
     */
    public List<ProviderMonthly> monthly(String serviceId, int months) {
        String url = routing.baseUrl(serviceId) + "/api/v1/consumption/monthly?months=" + months;
        try {
            List<ProviderMonthly> body = restClient.get().uri(url).retrieve()
                    .body(new ParameterizedTypeReference<List<ProviderMonthly>>() { });
            return body == null ? List.of() : body;
        } catch (RestClientException ex) {
            throw new ProviderUnavailableException(serviceId + " could not be reached: " + ex.getMessage(), ex);
        }
    }
}
