package com.sdem.city.api;

import com.sdem.city.bean.AggregateRow;
import com.sdem.city.bean.CitySummary;
import com.sdem.city.bean.ProviderStatus;
import com.sdem.city.service.AggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Version 1 of the Smart City API. */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Smart city", description = "Aggregated electricity data for one city")
public class SmartCityController {

    private final AggregationService service;

    public SmartCityController(AggregationService service) {
        this.service = service;
    }

    @GetMapping("/aggregates")
    @Operation(summary = "Stored monthly totals per provider for the last N months")
    public List<AggregateRow> aggregates(@RequestParam(defaultValue = "12") int months) {
        return service.aggregates(months);
    }

    @GetMapping("/summary")
    @Operation(summary = "City totals per provider, with each provider's collection status")
    public CitySummary summary(@RequestParam(defaultValue = "12") int months) {
        return service.summary(months);
    }

    @PostMapping("/aggregates/collect")
    @Operation(summary = "Collect from all providers now instead of waiting for the schedule")
    public List<ProviderStatus> collect() {
        return service.collectAll();
    }

    @GetMapping("/providers/status")
    @Operation(summary = "Result of the most recent collection from each provider")
    public List<ProviderStatus> statuses() {
        return service.statuses();
    }
}
