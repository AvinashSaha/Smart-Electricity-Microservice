package com.sdem.citizen.api;

import com.sdem.citizen.bean.CitizenBill;
import com.sdem.citizen.bean.CitizenRequest;
import com.sdem.citizen.bean.CitizenResponse;
import com.sdem.citizen.bean.ConsumptionSummary;
import com.sdem.citizen.bean.ReadingRequest;
import com.sdem.citizen.bean.ReadingResponse;
import com.sdem.citizen.service.CitizenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/citizens")
@Tag(name = "Citizens", description = "Households, smart meter readings, consumption and bills")
public class CitizenController {

    private final CitizenService service;

    public CitizenController(CitizenService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all citizens")
    public List<CitizenResponse> list() {
        return service.findAll().stream().map(CitizenResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one citizen")
    public CitizenResponse get(@PathVariable Long id) {
        return CitizenResponse.from(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a citizen (checks the provider exists)")
    public CitizenResponse register(@Valid @RequestBody CitizenRequest request) {
        return CitizenResponse.from(service.register(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a citizen")
    public CitizenResponse update(@PathVariable Long id, @Valid @RequestBody CitizenRequest request) {
        return CitizenResponse.from(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a citizen and their readings")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/provider/{providerId}")
    @Operation(summary = "Switch the citizen to another electricity provider")
    public CitizenResponse switchProvider(@PathVariable Long id, @PathVariable Long providerId) {
        return CitizenResponse.from(service.switchProvider(id, providerId));
    }

    @PostMapping("/{id}/readings")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a smart meter reading")
    public ReadingResponse addReading(@PathVariable Long id, @Valid @RequestBody ReadingRequest request) {
        return ReadingResponse.from(service.addReading(id, request));
    }

    @GetMapping("/{id}/readings")
    @Operation(summary = "List a citizen's meter readings, oldest first")
    public List<ReadingResponse> readings(@PathVariable Long id) {
        return service.readingsFor(id).stream().map(ReadingResponse::from).toList();
    }

    @GetMapping("/{id}/consumption")
    @Operation(summary = "Energy used between the first and latest reading")
    public ConsumptionSummary consumption(@PathVariable Long id) {
        return service.consumption(id);
    }

    @GetMapping("/{id}/bill")
    @Operation(summary = "Bill for the citizen, priced by their provider")
    public CitizenBill bill(@PathVariable Long id) {
        return service.bill(id);
    }

    @GetMapping("/consumption")
    @Operation(summary = "Consumption for every citizen (used by the Smart City service)")
    public List<ConsumptionSummary> consumptionForAll() {
        return service.consumptionForAll();
    }
}
