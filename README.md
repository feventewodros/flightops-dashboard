# ✈️ FlightOps Dashboard

A **full-stack web front end** for the [FlightOps API](../flightops-api) — a server-rendered
**Spring Boot + Thymeleaf + Bootstrap** application that consumes the REST API over HTTP, carries
the user's **JWT in the session**, and lets dispatchers search flights and book seats through a UI.

> Built by Feven Tewodros. Demonstrates full-stack development and **service-to-service integration**:
> this app owns no database — it talks to the FlightOps API like a real front end talks to a backend.

**Live demo:** _[deploying — link goes here]_

---

## What it shows
- **Server-side rendering** with Thymeleaf fragments (shared layout, navbar, flash messages).
- **REST integration** via Spring's `RestClient` — search, flight lookup, login, and booking calls.
- **Session-based auth** — logs in against the API, stores the returned JWT in the HTTP session, and attaches it as a `Bearer` token on protected calls.
- **Real UX** — a live flight board with delay badges and status colors, a search filter, a login screen, a booking form, flash success/error messages, and a friendly error page.
- **Tested** — `@WebMvcTest` slice tests with the API client mocked (board rendering, login success/failure, auth-gated booking).

## Architecture
```
Browser ──HTTP──▶ FlightOps Dashboard (Thymeleaf, :8090)
                        │  RestClient + Bearer JWT
                        ▼
                 FlightOps API (Spring Boot, :8080) ──▶ PostgreSQL
```

## Run it locally
You need the [FlightOps API](../flightops-api) running first (defaults to `http://localhost:8080`):

```bash
# terminal 1 — start the API
cd ../flightops-api && mvn spring-boot:run

# terminal 2 — start the dashboard
mvn spring-boot:run
```
Open **http://localhost:8090**. Sign in with `dispatcher / dispatch123` and book a seat.

Point at a different API with an env var:
```bash
API_BASE_URL=https://your-deployed-api.example.com mvn spring-boot:run
```

## Tech stack
Java 17 · Spring Boot 3.2 · Spring MVC · Thymeleaf · Bootstrap 5 · Spring `RestClient` · JUnit 5 · Maven · Docker

## Run the tests
```bash
mvn test
```

## License
MIT
