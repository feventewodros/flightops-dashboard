package com.feven.flightopsweb.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthResult(String token, String username, String role, long expiresInMs) {
}
