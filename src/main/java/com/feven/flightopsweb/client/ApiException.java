package com.feven.flightopsweb.client;

/** Thrown when the FlightOps API returns an error; carries a UI-friendly message. */
public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
