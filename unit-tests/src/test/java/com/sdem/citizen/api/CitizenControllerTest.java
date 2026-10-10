package com.sdem.citizen.api;

import com.sdem.citizen.entities.Citizen;
import com.sdem.citizen.entities.MeterType;
import com.sdem.citizen.exception.InvalidReadingException;
import com.sdem.citizen.service.CitizenService;
import com.sdem.citizen.service.SimulationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CitizenController.class)
class CitizenControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CitizenService citizens;

    @MockBean
    private SimulationService simulation;

    @Test
    void manualEntryIsForwarded() throws Exception {
        Citizen c = new Citizen("NEWCASTLE-A-M001", "A", MeterType.MANUAL, new BigDecimal("100"));
        c.accept(new BigDecimal("150"), LocalDateTime.of(2026, 10, 10, 12, 0));
        when(citizens.submitManual(eq("NEWCASTLE-A-M001"), any(BigDecimal.class))).thenReturn(c);

        mvc.perform(post("/api/v1/citizens/NEWCASTLE-A-M001/readings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"readingKwh\":150}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lastReadingKwh").value(150));
    }

    @Test
    void lowerReadingIsRejectedWith422() throws Exception {
        when(citizens.submitManual(eq("NEWCASTLE-A-M001"), any(BigDecimal.class)))
                .thenThrow(new InvalidReadingException("must be more"));

        mvc.perform(post("/api/v1/citizens/NEWCASTLE-A-M001/readings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"readingKwh\":1}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void missingValueIsRejectedWith400() throws Exception {
        mvc.perform(post("/api/v1/citizens/NEWCASTLE-A-M001/readings").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
