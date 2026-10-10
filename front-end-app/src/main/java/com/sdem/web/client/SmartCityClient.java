package com.sdem.web.client;

import com.sdem.web.bean.AggregateRow;
import com.sdem.web.config.FrontEndProperties;
import com.sdem.web.exception.ServiceUnavailableException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * REST client for the Smart City microservices. Every request goes through
 * the API gateway to {@code /smartcity-{city}/api/v1/...}.
 */
@Component
public class SmartCityClient {

    private final RestClient restClient;
    private final FrontEndProperties properties;

    public SmartCityClient(RestClient.Builder builder, FrontEndProperties properties) {
        this.restClient = builder.build();
        this.properties = properties;
    }

    /** Stored monthly aggregates of one city for the last {@code months} months. */
    public List<AggregateRow> aggregates(FrontEndProperties.City city, int months) {
        String url = properties.baseUrl(city.smartCityServiceId()) + "/api/" + properties.apiVersion()
                + "/aggregates?months=" + months;
        try {
            List<AggregateRow> rows = restClient.get().uri(url).retrieve()
                    .body(new ParameterizedTypeReference<List<AggregateRow>>() { });
            return rows == null ? List.of() : rows;
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException(city.name() + " smart city service is unavailable", ex);
        }
    }
}
