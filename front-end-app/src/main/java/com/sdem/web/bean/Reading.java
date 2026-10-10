package com.sdem.web.bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Reading(Long id, BigDecimal readingKwh, LocalDateTime readingAt) {
}
