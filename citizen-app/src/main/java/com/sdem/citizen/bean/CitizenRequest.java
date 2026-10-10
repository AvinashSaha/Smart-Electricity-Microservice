package com.sdem.citizen.bean;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Payload used to register or update a citizen (household). */
public record CitizenRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{1,2}[0-9][A-Za-z0-9]? ?[0-9][A-Za-z]{2}$",
                message = "must be a valid UK postcode")
        String postcode,
        @NotBlank String meterId,
        @NotNull Long providerId) {
}
