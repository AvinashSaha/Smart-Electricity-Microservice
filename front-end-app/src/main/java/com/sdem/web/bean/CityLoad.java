package com.sdem.web.bean;

import com.sdem.web.config.FrontEndProperties;

import java.util.List;

/**
 * Result of loading one city's data: either rows, or the error that stopped it.
 */
public record CityLoad(FrontEndProperties.City city, List<AggregateRow> rows, String error) {

    public boolean available() {
        return error == null;
    }
}
