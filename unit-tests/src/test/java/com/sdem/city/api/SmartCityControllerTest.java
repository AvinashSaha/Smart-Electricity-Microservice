package com.sdem.city.api;

import com.sdem.city.bean.AggregateRow;
import com.sdem.city.service.AggregationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SmartCityController.class)
class SmartCityControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AggregationService service;

    @Test
    void returnsStoredAggregates() throws Exception {
        when(service.aggregates(12)).thenReturn(List.of(
                new AggregateRow("Newcastle", "A", "2026-10", new BigDecimal("123.4"), 10, 5, null)));

        mvc.perform(get("/api/v1/aggregates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].provider").value("A"))
                .andExpect(jsonPath("$[0].totalKwh").value(123.4));
    }

    @Test
    void returns400ForABadMonthCount() throws Exception {
        when(service.aggregates(0)).thenThrow(new IllegalArgumentException("months must be between 1 and 60"));

        mvc.perform(get("/api/v1/aggregates").param("months", "0")).andExpect(status().isBadRequest());
    }
}
