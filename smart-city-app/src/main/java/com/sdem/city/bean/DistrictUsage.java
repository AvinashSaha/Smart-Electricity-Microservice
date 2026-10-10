package com.sdem.city.bean;

import java.math.BigDecimal;

/** Usage grouped by postcode district, for example "NE4". */
public record DistrictUsage(String district, long citizens, BigDecimal kwh, BigDecimal averageKwh) {
}
