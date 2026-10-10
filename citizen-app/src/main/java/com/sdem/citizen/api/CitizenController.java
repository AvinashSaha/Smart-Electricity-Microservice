package com.sdem.citizen.api;

import com.sdem.citizen.bean.CitizenResponse;
import com.sdem.citizen.bean.ManualReadingRequest;
import com.sdem.citizen.bean.SimulationStatus;
import com.sdem.citizen.service.CitizenService;
import com.sdem.citizen.service.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Version 1 of the Citizen API. */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Citizens", description = "Simulated citizens and smart meters")
public class CitizenController {

    private final CitizenService citizens;
    private final SimulationService simulation;

    public CitizenController(CitizenService citizens, SimulationService simulation) {
        this.citizens = citizens;
        this.simulation = simulation;
    }

    @GetMapping("/citizens")
    @Operation(summary = "List citizens and smart meters, optionally for one provider")
    public List<CitizenResponse> list(@RequestParam(required = false) String provider) {
        return citizens.list(provider).stream().map(CitizenResponse::from).toList();
    }

    @GetMapping("/citizens/{id}")
    @Operation(summary = "Get one citizen or smart meter")
    public CitizenResponse get(@PathVariable String id) {
        return CitizenResponse.from(citizens.get(id));
    }

    @PostMapping("/citizens/{id}/readings")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Manual entry: a citizen submits a reading, forwarded to their provider in real time")
    public CitizenResponse submit(@PathVariable String id, @Valid @RequestBody ManualReadingRequest request) {
        return CitizenResponse.from(citizens.submitManual(id, request.readingKwh()));
    }

    @GetMapping("/simulation/status")
    @Operation(summary = "Simulation counters")
    public SimulationStatus status() {
        return simulation.status();
    }
}
