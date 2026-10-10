package com.sdem.web.client;

import com.sdem.web.bean.Bill;
import com.sdem.web.bean.Citizen;
import com.sdem.web.bean.CitizenForm;
import com.sdem.web.bean.CitySummary;
import com.sdem.web.bean.Consumption;
import com.sdem.web.bean.Provider;
import com.sdem.web.bean.ProviderForm;
import com.sdem.web.bean.Reading;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** All calls from the front end go through the SDEM API gateway. */
@Component
public class GatewayClient {

    private final RestClient rest;

    public GatewayClient(RestClient.Builder builder,
                         @Value("${sdem.gateway.url:http://localhost:8080}") String gatewayUrl) {
        this.rest = builder.baseUrl(gatewayUrl).build();
    }

    // ---- Providers --------------------------------------------------------

    public List<Provider> providers() {
        return call(() -> rest.get().uri("/api/providers").retrieve()
                .body(new ParameterizedTypeReference<List<Provider>>() { }));
    }

    public Provider createProvider(ProviderForm form) {
        return call(() -> rest.post().uri("/api/providers").contentType(MediaType.APPLICATION_JSON)
                .body(form).retrieve().body(Provider.class));
    }

    public void deactivateProvider(Long id) {
        call(() -> rest.delete().uri("/api/providers/{id}", id).retrieve().toBodilessEntity());
    }

    // ---- Citizens ---------------------------------------------------------

    public List<Citizen> citizens() {
        return call(() -> rest.get().uri("/api/citizens").retrieve()
                .body(new ParameterizedTypeReference<List<Citizen>>() { }));
    }

    public Citizen citizen(Long id) {
        return call(() -> rest.get().uri("/api/citizens/{id}", id).retrieve().body(Citizen.class));
    }

    public Citizen registerCitizen(CitizenForm form) {
        return call(() -> rest.post().uri("/api/citizens").contentType(MediaType.APPLICATION_JSON)
                .body(form).retrieve().body(Citizen.class));
    }

    public List<Reading> readings(Long citizenId) {
        return call(() -> rest.get().uri("/api/citizens/{id}/readings", citizenId).retrieve()
                .body(new ParameterizedTypeReference<List<Reading>>() { }));
    }

    public void addReading(Long citizenId, BigDecimal readingKwh) {
        call(() -> rest.post().uri("/api/citizens/{id}/readings", citizenId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("readingKwh", readingKwh))
                .retrieve().toBodilessEntity());
    }

    public Consumption consumption(Long citizenId) {
        return call(() -> rest.get().uri("/api/citizens/{id}/consumption", citizenId).retrieve()
                .body(Consumption.class));
    }

    public Bill bill(Long citizenId) {
        return call(() -> rest.get().uri("/api/citizens/{id}/bill", citizenId).retrieve().body(Bill.class));
    }

    public void switchProvider(Long citizenId, Long providerId) {
        call(() -> rest.put().uri("/api/citizens/{id}/provider/{providerId}", citizenId, providerId)
                .retrieve().toBodilessEntity());
    }

    // ---- Smart city -------------------------------------------------------

    public CitySummary citySummary() {
        return call(() -> rest.get().uri("/api/city/summary").retrieve().body(CitySummary.class));
    }

    public void takeSnapshot() {
        call(() -> rest.post().uri("/api/city/snapshots").retrieve().toBodilessEntity());
    }

    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientResponseException ex) {
            throw new GatewayException(readableMessage(ex), ex);
        } catch (RestClientException ex) {
            throw new GatewayException("The SDEM API gateway could not be reached. Is it running?", ex);
        }
    }

    private static String readableMessage(RestClientResponseException ex) {
        try {
            ProblemDetail problem = ex.getResponseBodyAs(ProblemDetail.class);
            if (problem != null && problem.getDetail() != null) {
                return problem.getDetail();
            }
        } catch (RuntimeException ignored) {
            // fall through to the generic message
        }
        return "Request failed with status " + ex.getStatusCode().value();
    }
}
