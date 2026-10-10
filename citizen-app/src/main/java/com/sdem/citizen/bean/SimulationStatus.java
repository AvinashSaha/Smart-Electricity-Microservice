package com.sdem.citizen.bean;

import java.time.LocalDateTime;

/**
 * Counters describing the simulation.
 *
 * @param city             city id
 * @param manualCitizens   citizens entering readings by hand
 * @param smartMeters      smart meters
 * @param pendingBackfill  citizens whose history has not been sent yet
 * @param ticks            simulation ticks run since start
 * @param readingsSent     readings accepted by providers since start
 * @param failures         readings that could not be delivered since start
 * @param lastTickAt       time of the last tick
 */
public record SimulationStatus(String city, long manualCitizens, long smartMeters, long pendingBackfill,
                               long ticks, long readingsSent, long failures, LocalDateTime lastTickAt) {
}
