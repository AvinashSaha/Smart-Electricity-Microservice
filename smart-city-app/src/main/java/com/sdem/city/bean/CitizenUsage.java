package com.sdem.city.bean;

import java.math.BigDecimal;

/** Consumption for one citizen, as returned by the Citizen service. Extra fields are ignored. */
public record CitizenUsage(Long citizenId, String postcode, Long providerId, int days, BigDecimal kwh) {
}
