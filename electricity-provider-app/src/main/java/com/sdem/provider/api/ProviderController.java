package com.sdem.provider.api;

import com.sdem.provider.bean.MonthlyConsumption;
import com.sdem.provider.bean.ProviderSummary;
import com.sdem.provider.bean.ReadingRequest;
import com.sdem.provider.bean.ReadingResponse;
import com.sdem.provider.service.ReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Version 1 of the Electricity Provider API. */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Electricity provider", description = "Receives readings in real time and reports consumption")
public class ProviderController {

    private final ReadingService service;

    public ProviderController(ReadingService service) {
        this.service = service;
    }

    @PostMapping("/readings")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Receive a reading from a citizen (manual) or smart meter")
    public ReadingResponse receive(@Valid @RequestBody ReadingRequest request) {
        return ReadingResponse.from(service.receive(request));
    }

    @GetMapping("/readings")
    @Operation(summary = "Most recent readings, optionally for one citizen")
    public List<ReadingResponse> latest(@RequestParam(required = false) String citizenId,
                                        @RequestParam(defaultValue = "50") int limit) {
        return service.latest(citizenId, limit).stream().map(ReadingResponse::from).toList();
    }

    @GetMapping("/consumption/monthly")
    @Operation(summary = "Monthly totals (kWh, readings, citizens) for the last N months")
    public List<MonthlyConsumption> monthly(@RequestParam(defaultValue = "12") int months) {
        return service.monthly(months);
    }

    @GetMapping("/summary")
    @Operation(summary = "All time totals for this provider")
    public ProviderSummary summary() {
        return service.summary();
    }
}
