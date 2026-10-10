package com.sdem.web.bean;

public record Citizen(Long id, String fullName, String email, String postcode, String meterId, Long providerId) {
}
