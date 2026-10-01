package com.feven.flightopsweb.client;

import com.feven.flightopsweb.client.dto.AuthResult;
import com.feven.flightopsweb.client.dto.BookingView;
import com.feven.flightopsweb.client.dto.FlightView;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Thin HTTP client over the FlightOps REST API. */
@Component
public class FlightOpsClient {

    private final RestClient rest;

    public FlightOpsClient(RestClient flightOpsRestClient) {
        this.rest = flightOpsRestClient;
    }

    public List<FlightView> searchFlights(String origin, String destination) {
        return withWakeRetry(() -> rest.get()
                .uri(uri -> uri.path("/api/flights")
                        .queryParamIfPresent("origin", opt(origin))
                        .queryParamIfPresent("destination", opt(destination))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<FlightView>>() {}),
                "Could not load flights");
    }

    public FlightView getFlight(Long id) {
        return withWakeRetry(() -> rest.get()
                .uri("/api/flights/{id}", id)
                .retrieve()
                .body(FlightView.class),
                "Flight " + id + " not found");
    }

    public AuthResult login(String username, String password) {
        return rest.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", username, "password", password))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new ApiException("Invalid username or password");
                })
                .body(AuthResult.class);
    }

    public BookingView book(String token, Long flightId, String passengerName, String seatNumber) {
        return rest.post()
                .uri("/api/bookings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "flightId", flightId,
                        "passengerName", passengerName,
                        "seatNumber", seatNumber == null ? "" : seatNumber))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new ApiException(extractMessage(body, res.getStatusCode().toString()));
                })
                .body(BookingView.class);
    }

    /**
     * Runs an API call, retrying while the API is still waking from a free-tier cold start
     * (it returns 5xx or refuses the connection for ~50s). Gives up after ~60s with a friendly error.
     */
    private <T> T withWakeRetry(Supplier<T> call, String failMsg) {
        final int attempts = 10;
        for (int i = 0; i < attempts; i++) {
            try {
                return call.get();
            } catch (RestClientResponseException e) {
                boolean waking = e.getStatusCode().is5xxServerError();
                if (!waking || i == attempts - 1) {
                    throw new ApiException(failMsg + " (" + e.getStatusCode() + ")");
                }
                sleep(6000);
            } catch (ResourceAccessException e) {
                if (i == attempts - 1) {
                    throw new ApiException(failMsg + " (service unreachable)");
                }
                sleep(6000);
            }
        }
        throw new ApiException(failMsg);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request interrupted");
        }
    }

    private java.util.Optional<String> opt(String s) {
        return StringUtils.hasText(s) ? java.util.Optional.of(s.trim()) : java.util.Optional.empty();
    }

    /** Pull the "message" field out of the API's JSON error body if present. */
    private String extractMessage(String body, String fallback) {
        int i = body.indexOf("\"message\"");
        if (i >= 0) {
            int colon = body.indexOf(':', i);
            int start = body.indexOf('"', colon + 1);
            int end = body.indexOf('"', start + 1);
            if (start >= 0 && end > start) {
                return body.substring(start + 1, end);
            }
        }
        return "Booking failed (" + fallback + ")";
    }
}
