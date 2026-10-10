package com.sdem.citizen.api;

import com.sdem.citizen.bean.ProviderInfo;
import com.sdem.citizen.bean.ProviderQuote;
import com.sdem.citizen.service.ProviderClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Boots the citizen service against the seeded H2 database. The provider
 * service is replaced with a mock so the test runs on its own.
 */
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class CitizenControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProviderClient providerClient;

    @Test
    void listsSeededCitizens() throws Exception {
        mvc.perform(get("/api/citizens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Amelia Clarke"));
    }

    @Test
    void calculatesConsumptionFromReadings() throws Exception {
        mvc.perform(get("/api/citizens/1/consumption"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readingCount").value(4))
                .andExpect(jsonPath("$.days").value(30));
    }

    @Test
    void billsUsingTheProviderQuote() throws Exception {
        when(providerClient.quote(anyLong(), any(BigDecimal.class), anyInt())).thenReturn(
                new ProviderQuote(1L, "Northern Power Co", new BigDecimal("300"), 30,
                        new BigDecimal("73.50"), new BigDecimal("16.01"), new BigDecimal("89.51"),
                        new BigDecimal("105")));

        mvc.perform(get("/api/citizens/1/bill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quote.providerName").value("Northern Power Co"))
                .andExpect(jsonPath("$.quote.total").value(89.51));
    }

    @Test
    void registersANewCitizen() throws Exception {
        when(providerClient.getProvider(2L)).thenReturn(new ProviderInfo(2L, "GreenGrid Energy",
                new BigDecimal("26.10"), new BigDecimal("48.20"), 100, true));

        mvc.perform(post("/api/citizens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"New Resident","email":"new.resident@example.com",
                                 "postcode":"ne1 7ru","meterId":"SM-2001","providerId":2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postcode").value("NE1 7RU"));
    }

    @Test
    void rejectsInvalidPostcode() throws Exception {
        mvc.perform(post("/api/citizens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Bad Postcode","email":"bad@example.com",
                                 "postcode":"12345","meterId":"SM-2002","providerId":1}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns404ForUnknownCitizen() throws Exception {
        mvc.perform(get("/api/citizens/999")).andExpect(status().isNotFound());
    }
}
