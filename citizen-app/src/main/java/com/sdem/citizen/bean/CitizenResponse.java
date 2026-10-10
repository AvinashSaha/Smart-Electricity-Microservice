package com.sdem.citizen.bean;

import com.sdem.citizen.entities.Citizen;

public record CitizenResponse(
        Long id,
        String fullName,
        String email,
        String postcode,
        String meterId,
        Long providerId) {

    public static CitizenResponse from(Citizen c) {
        return new CitizenResponse(c.getId(), c.getFullName(), c.getEmail(),
                c.getPostcode(), c.getMeterId(), c.getProviderId());
    }
}
