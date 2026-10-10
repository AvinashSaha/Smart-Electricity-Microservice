package com.sdem.city.bean;

import java.time.LocalDateTime;

/**
 * Result of the most recent collection from one provider.
 *
 * @param provider      provider code, for example "A"
 * @param serviceId     service id used to reach it through the gateway
 * @param reachable     whether the last request succeeded
 * @param lastAttemptAt when the last request was made
 * @param lastSuccessAt when data was last collected successfully
 * @param message       error message of the last failure, if any
 */
public record ProviderStatus(
        String provider,
        String serviceId,
        boolean reachable,
        LocalDateTime lastAttemptAt,
        LocalDateTime lastSuccessAt,
        String message) {
}
