package com.sdem.web.api;

import com.sdem.web.bean.Bill;
import com.sdem.web.bean.Citizen;
import com.sdem.web.bean.CitySummary;
import com.sdem.web.bean.Consumption;
import com.sdem.web.bean.Provider;
import com.sdem.web.bean.Quote;
import com.sdem.web.bean.Reading;
import com.sdem.web.client.GatewayClient;
import com.sdem.web.client.GatewayException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Renders each Thymeleaf page with the gateway mocked out. */
@WebMvcTest({DashboardController.class, ProviderPageController.class, CitizenPageController.class})
class PageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GatewayClient gateway;

    private final Provider provider = new Provider(1L, "GreenGrid Energy",
            new BigDecimal("26.10"), new BigDecimal("48.20"), 100, true);

    @Test
    void rendersDashboard() throws Exception {
        when(gateway.citySummary()).thenReturn(new CitySummary(LocalDateTime.now(), 2,
                new BigDecimal("600"), new BigDecimal("300"), new BigDecimal("300"),
                new BigDecimal("50.0"), new BigDecimal("62.10"),
                List.of(new CitySummary.ProviderUsage(1L, "GreenGrid Energy", 100, 1,
                        new BigDecimal("300"), new BigDecimal("300"))),
                List.of(new CitySummary.DistrictUsage("NE4", 2, new BigDecimal("600"), new BigDecimal("300")))));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("GreenGrid Energy")))
                .andExpect(content().string(containsString("NE4")));
    }

    @Test
    void dashboardShowsErrorWhenGatewayIsDown() throws Exception {
        when(gateway.citySummary()).thenThrow(new GatewayException("The SDEM API gateway could not be reached", null));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("could not be reached")));
    }

    @Test
    void rendersProviders() throws Exception {
        when(gateway.providers()).thenReturn(List.of(provider));

        mvc.perform(get("/providers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("GreenGrid Energy")));
    }

    @Test
    void rendersCitizenDetailWithBill() throws Exception {
        when(gateway.providers()).thenReturn(List.of(provider));
        when(gateway.citizen(1L)).thenReturn(new Citizen(1L, "Amelia Clarke", "amelia@example.com",
                "NE4 7PL", "SM-1001", 1L));
        when(gateway.readings(1L)).thenReturn(List.of(
                new Reading(1L, new BigDecimal("1000"), LocalDateTime.of(2026, 9, 1, 8, 0)),
                new Reading(2L, new BigDecimal("1300"), LocalDateTime.of(2026, 10, 1, 8, 0))));
        Consumption usage = new Consumption(1L, "NE4 7PL", 1L, 2,
                LocalDateTime.of(2026, 9, 1, 8, 0), LocalDateTime.of(2026, 10, 1, 8, 0), 30, new BigDecimal("300"));
        when(gateway.consumption(1L)).thenReturn(usage);
        when(gateway.bill(1L)).thenReturn(new Bill(1L, "Amelia Clarke", usage,
                new Quote(1L, "GreenGrid Energy", new BigDecimal("300"), 30, new BigDecimal("78.30"),
                        new BigDecimal("14.46"), new BigDecimal("92.76"), new BigDecimal("300"))));

        mvc.perform(get("/citizens/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Amelia Clarke")))
                .andExpect(content().string(containsString("92.76")));
    }

    @Test
    void rendersCitizenList() throws Exception {
        when(gateway.providers()).thenReturn(List.of(provider));
        when(gateway.citizens()).thenReturn(List.of(new Citizen(1L, "Amelia Clarke", "amelia@example.com",
                "NE4 7PL", "SM-1001", 1L)));

        mvc.perform(get("/citizens"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Amelia Clarke")));
    }
}
