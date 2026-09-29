package com.feven.flightopsweb.web;

import com.feven.flightopsweb.client.ApiException;
import com.feven.flightopsweb.client.FlightOpsClient;
import com.feven.flightopsweb.client.dto.AuthResult;
import com.feven.flightopsweb.client.dto.BookingView;
import com.feven.flightopsweb.client.dto.FlightView;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class DashboardController {

    private static final String SESSION_TOKEN = "jwt";
    private static final String SESSION_USER = "username";

    private final FlightOpsClient api;

    public DashboardController(FlightOpsClient api) {
        this.api = api;
    }

    @GetMapping("/")
    public String board(@RequestParam(required = false) String origin,
                        @RequestParam(required = false) String destination,
                        HttpSession session, Model model) {
        List<FlightView> flights = api.searchFlights(origin, destination);
        model.addAttribute("flights", flights);
        model.addAttribute("origin", origin == null ? "" : origin);
        model.addAttribute("destination", destination == null ? "" : destination);
        model.addAttribute("username", session.getAttribute(SESSION_USER));
        return "index";
    }

    @GetMapping("/login")
    public String loginForm(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute(SESSION_USER));
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpSession session, RedirectAttributes ra) {
        try {
            AuthResult auth = api.login(username, password);
            session.setAttribute(SESSION_TOKEN, auth.token());
            session.setAttribute(SESSION_USER, auth.username());
            ra.addFlashAttribute("success", "Signed in as " + auth.username() + " (" + auth.role() + ")");
            return "redirect:/";
        } catch (ApiException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addFlashAttribute("success", "Signed out");
        return "redirect:/";
    }

    @GetMapping("/book/{flightId}")
    public String bookForm(@PathVariable Long flightId, HttpSession session, Model model,
                           RedirectAttributes ra) {
        if (session.getAttribute(SESSION_TOKEN) == null) {
            ra.addFlashAttribute("error", "Please sign in to book a seat");
            return "redirect:/login";
        }
        FlightView flight = api.getFlight(flightId);
        model.addAttribute("flight", flight);
        model.addAttribute("username", session.getAttribute(SESSION_USER));
        return "book";
    }

    @PostMapping("/book")
    public String book(@RequestParam Long flightId,
                       @RequestParam String passengerName,
                       @RequestParam(required = false) String seatNumber,
                       HttpSession session, RedirectAttributes ra) {
        String token = (String) session.getAttribute(SESSION_TOKEN);
        if (token == null) {
            ra.addFlashAttribute("error", "Please sign in to book a seat");
            return "redirect:/login";
        }
        try {
            BookingView booking = api.book(token, flightId, passengerName, seatNumber);
            ra.addFlashAttribute("success",
                    "Booked seat for " + booking.passengerName() + " on " + booking.flightNumber()
                            + " — confirmation " + booking.bookingRef());
            return "redirect:/";
        } catch (ApiException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/book/" + flightId;
        }
    }

    /** Any uncaught API failure renders a friendly error page instead of a stack trace. */
    @ExceptionHandler(ApiException.class)
    public String handleApiError(ApiException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error-page";
    }
}
