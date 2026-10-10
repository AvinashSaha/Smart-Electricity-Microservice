package com.sdem.web.bean;

public record Bill(Long citizenId, String fullName, Consumption consumption, Quote quote) {
}
