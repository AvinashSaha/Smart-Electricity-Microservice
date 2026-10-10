package com.sdem.citizen.bean;

/** A citizen's bill: their consumption combined with their provider's quote. */
public record CitizenBill(
        Long citizenId,
        String fullName,
        ConsumptionSummary consumption,
        ProviderQuote quote) {
}
