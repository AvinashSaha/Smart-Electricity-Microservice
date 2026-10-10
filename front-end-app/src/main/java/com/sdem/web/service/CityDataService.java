package com.sdem.web.service;

import com.sdem.web.bean.AggregateRow;
import com.sdem.web.bean.CityLoad;
import com.sdem.web.client.SmartCityClient;
import com.sdem.web.config.FrontEndProperties;
import com.sdem.web.exception.ServiceUnavailableException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Loads data from the city modules. A city whose service is down is returned
 * with an error message instead of failing the whole page.
 */
@Service
public class CityDataService {

    private static final DateTimeFormatter LABEL = DateTimeFormatter.ofPattern("MMM yyyy", Locale.UK);

    private final SmartCityClient client;
    private final FrontEndProperties properties;
    private final Clock clock;

    public CityDataService(SmartCityClient client, FrontEndProperties properties, Clock clock) {
        this.client = client;
        this.properties = properties;
        this.clock = clock;
    }

    public CityLoad load(FrontEndProperties.City city, int months) {
        try {
            return new CityLoad(city, client.aggregates(city, months), null);
        } catch (ServiceUnavailableException ex) {
            return new CityLoad(city, List.of(), ex.getMessage());
        }
    }

    public List<CityLoad> loadAll(int months) {
        return properties.cities().stream().map(c -> load(c, months)).toList();
    }

    /** Month keys (yyyy-MM) for the last {@code count} months, oldest first. */
    public List<String> monthKeys(int count) {
        YearMonth current = YearMonth.now(clock);
        List<String> keys = new ArrayList<>();
        for (int i = count - 1; i >= 0; i--) {
            keys.add(current.minusMonths(i).toString());
        }
        return keys;
    }

    /** "2026-03" becomes "Mar 2026". */
    public static String label(String monthKey) {
        return YearMonth.parse(monthKey).format(LABEL);
    }

    /** Finds the row for a provider and month, if the city sent one. */
    static AggregateRow find(List<AggregateRow> rows, String provider, String month) {
        for (AggregateRow r : rows) {
            if (r.provider().equalsIgnoreCase(provider) && r.month().equals(month)) {
                return r;
            }
        }
        return null;
    }

    /** Clamps the requested number of months to a sensible range. */
    public int months(Integer requested) {
        int value = requested == null ? properties.months() : requested;
        return Math.max(1, Math.min(value, 24));
    }
}
