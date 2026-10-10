package com.sdem.provider.api;

import com.sdem.provider.bean.MonthlyConsumption;
import com.sdem.provider.bean.ReadingRequest;
import com.sdem.provider.entities.MeterReading;
import com.sdem.provider.entities.MeterType;
import com.sdem.provider.exception.InvalidReadingException;
import com.sdem.provider.service.ReadingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProviderController.class)
class ProviderControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReadingService service;

    @Test
    void acceptsAValidReading() throws Exception {
        when(service.receive(any(ReadingRequest.class))).thenReturn(new MeterReading("NEWCASTLE-A-S001",
                MeterType.SMART, new BigDecimal("1200.5"), new BigDecimal("0.5"),
                LocalDateTime.of(2026, 10, 10, 9, 0), LocalDateTime.of(2026, 10, 10, 9, 0)));

        mvc.perform(post("/api/v1/readings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"citizenId\":\"NEWCASTLE-A-S001\",\"meterType\":\"SMART\",\"readingKwh\":1200.5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consumptionKwh").value(0.5));
    }

    @Test
    void rejectsAnIncompleteReading() throws Exception {
        mvc.perform(post("/api/v1/readings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"meterType\":\"SMART\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns422WhenTheReadingIsNotHigher() throws Exception {
        when(service.receive(any(ReadingRequest.class))).thenThrow(new InvalidReadingException("too low"));

        mvc.perform(post("/api/v1/readings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"citizenId\":\"C1\",\"meterType\":\"MANUAL\",\"readingKwh\":1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("too low"));
    }

    @Test
    void returnsMonthlyTotals() throws Exception {
        when(service.monthly(2)).thenReturn(List.of(
                new MonthlyConsumption("2026-09", new BigDecimal("100"), 10, 5),
                new MonthlyConsumption("2026-10", new BigDecimal("40"), 4, 2)));

        mvc.perform(get("/api/v1/consumption/monthly").param("months", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].month").value("2026-10"))
                .andExpect(jsonPath("$[1].citizenCount").value(2));
    }
}
