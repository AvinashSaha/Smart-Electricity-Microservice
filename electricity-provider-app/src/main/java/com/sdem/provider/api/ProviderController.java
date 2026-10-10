package com.sdem.provider.api;

import com.sdem.provider.bean.BillQuote;
import com.sdem.provider.bean.ProviderRequest;
import com.sdem.provider.bean.ProviderResponse;
import com.sdem.provider.service.ProviderService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/providers")
@Tag(name = "Electricity providers", description = "Suppliers, tariffs and bill quotes")
public class ProviderController {

    private final ProviderService service;

    public ProviderController(ProviderService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all active providers")
    public List<ProviderResponse> list() {
        return service.findAllActive().stream().map(ProviderResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one provider")
    public ProviderResponse get(@PathVariable Long id) {
        return ProviderResponse.from(service.findById(id));
    }

    @GetMapping("/cheapest")
    @Operation(summary = "Get the active provider with the lowest unit rate")
    public ProviderResponse cheapest() {
        return ProviderResponse.from(service.findCheapest());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new provider")
    public ProviderResponse create(@Valid @RequestBody ProviderRequest request) {
        return ProviderResponse.from(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a provider's tariff details")
    public ProviderResponse update(@PathVariable Long id, @Valid @RequestBody ProviderRequest request) {
        return ProviderResponse.from(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate a provider (soft delete)")
    public void delete(@PathVariable Long id) {
        service.deactivate(id);
    }

    @GetMapping("/{id}/quote")
    @Operation(summary = "Quote a bill for a given kWh over a number of days")
    public BillQuote quote(@PathVariable Long id,
                           @RequestParam BigDecimal kwh,
                           @RequestParam(defaultValue = "30") int days) {
        return service.quote(id, kwh, days);
    }
}
