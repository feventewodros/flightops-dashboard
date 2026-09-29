package com.feven.flightopsweb.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingView(
        String bookingRef,
        Long flightId,
        String flightNumber,
        String passengerName,
        String seatNumber) {
}
