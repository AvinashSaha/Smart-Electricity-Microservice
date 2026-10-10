package com.sdem.provider.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Boots the whole service against the seeded H2 database (Eureka disabled). */
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class ProviderControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void listsSeededProviders() throws Exception {
        mvc.perform(get("/api/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(4)));
    }

    @Test
    void returnsCheapestProvider() throws Exception {
        mvc.perform(get("/api/providers/cheapest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tyne Valley Electric"));
    }

    @Test
    void quotesABill() throws Exception {
        mvc.perform(get("/api/providers/1/quote").param("kwh", "100").param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.energyCost").value(24.50))
                .andExpect(jsonPath("$.total").value(40.51));
    }

    @Test
    void returns404ForUnknownProvider() throws Exception {
        mvc.perform(get("/api/providers/999")).andExpect(status().isNotFound());
    }

    @Test
    void validatesNewProvider() throws Exception {
        mvc.perform(post("/api/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"unitRatePence\":0,\"standingChargePence\":10,\"renewablePercent\":150}"))
                .andExpect(status().isBadRequest());
    }
}
