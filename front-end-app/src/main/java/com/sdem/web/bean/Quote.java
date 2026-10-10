package com.sdem.web.bean;

import java.math.BigDecimal;

public record Quote(Long providerId, String providerName, BigDecimal kwh, int days, BigDecimal energyCost,
                    BigDecimal standingCharge, BigDecimal total, BigDecimal renewableKwh) {
}
