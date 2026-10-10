package com.sdem.web.bean;

import java.math.BigDecimal;

public record Provider(Long id, String name, BigDecimal unitRatePence, BigDecimal standingChargePence,
                       int renewablePercent, boolean active) {
}
