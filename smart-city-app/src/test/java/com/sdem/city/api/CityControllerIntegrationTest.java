package com.sdem.city.api;

import com.sdem.city.bean.CitizenUsage;
import com.sdem.city.bean.ProviderInfo;
import com.sdem.city.service.DownstreamClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class CityControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DownstreamClient client;

    @BeforeEach
    void stubDownstreamServices() {
        when(client.providers()).thenReturn(List.of(
                new ProviderInfo(1L, "GreenGrid Energy", BigDecimal.TEN, BigDecimal.ONE, 100, true)));
        when(client.citizenUsage()).thenReturn(List.of(
                new CitizenUsage(1L, "NE4 5AB", 1L, 30, new BigDecimal("250"))));
    }

    @Test
    void returnsCitySummary() throws Exception {
        mvc.perform(get("/api/city/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citizenCount").value(1))
                .andExpect(jsonPath("$.renewableSharePercent").value(100.0))
                .andExpect(jsonPath("$.byDistrict[0].district").value("NE4"));
    }

    @Test
    void savesAndListsSnapshots() throws Exception {
        mvc.perform(post("/api/city/snapshots")).andExpect(status().isCreated());
        mvc.perform(get("/api/city/snapshots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].citizenCount").value(1));
    }
}
