package com.sdem.city.api;

import com.sdem.city.bean.CitySummary;
import com.sdem.city.bean.DistrictUsage;
import com.sdem.city.bean.ProviderUsage;
import com.sdem.city.entities.CitySnapshot;
import com.sdem.city.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/city")
@Tag(name = "Smart city", description = "City wide electricity reports")
public class CityController {

    private final CityService service;

    public CityController(CityService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    @Operation(summary = "Live city report: totals, renewable share, CO2 avoided, per provider and per district")
    public CitySummary summary() {
        return service.summary();
    }

    @GetMapping("/districts")
    @Operation(summary = "Usage grouped by postcode district")
    public List<DistrictUsage> districts() {
        return service.summary().byDistrict();
    }

    @GetMapping("/providers")
    @Operation(summary = "Usage grouped by electricity provider")
    public List<ProviderUsage> providers() {
        return service.summary().byProvider();
    }

    @PostMapping("/snapshots")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save the current headline figures as a snapshot")
    public CitySnapshot takeSnapshot() {
        return service.takeSnapshot();
    }

    @GetMapping("/snapshots")
    @Operation(summary = "Saved snapshots, newest first")
    public List<CitySnapshot> history() {
        return service.history();
    }
}
