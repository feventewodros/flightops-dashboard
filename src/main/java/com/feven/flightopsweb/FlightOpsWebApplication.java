package com.feven.flightopsweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FlightOps Dashboard — a server-rendered (Thymeleaf) web front end that consumes the
 * FlightOps REST API over HTTP. Demonstrates full-stack development and service-to-service
 * integration with JWT auth carried in the user's session.
 */
@SpringBootApplication
public class FlightOpsWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlightOpsWebApplication.class, args);
    }
}
