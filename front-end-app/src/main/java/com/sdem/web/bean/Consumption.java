package com.sdem.web.bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Consumption(Long citizenId, String postcode, Long providerId, int readingCount,
                          LocalDateTime from, LocalDateTime to, int days, BigDecimal kwh) {
}
