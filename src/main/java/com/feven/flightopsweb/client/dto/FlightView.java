package com.feven.flightopsweb.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/** Subset of the FlightOps API flight response used by the UI. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FlightView(
        Long id,
        String flightNumber,
        String aircraft,
        String origin,
        String destination,
        LocalDateTime scheduledDeparture,
        LocalDateTime estimatedDeparture,
        String status,
        int delayMinutes,
        int availableSeats) {
}
