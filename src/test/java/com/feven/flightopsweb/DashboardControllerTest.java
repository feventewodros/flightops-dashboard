package com.feven.flightopsweb;

import com.feven.flightopsweb.client.ApiException;
import com.feven.flightopsweb.client.FlightOpsClient;
import com.feven.flightopsweb.client.dto.AuthResult;
import com.feven.flightopsweb.client.dto.FlightView;
import com.feven.flightopsweb.web.DashboardController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired MockMvc mvc;

    @MockBean FlightOpsClient api;

    @Test
    void boardRendersFlightsFromApi() throws Exception {
        when(api.searchFlights(any(), any())).thenReturn(List.of(
                new FlightView(1L, "UA1201", "N738DN (Boeing 737-800)", "DEN", "LAX",
                        LocalDateTime.now(), LocalDateTime.now(), "SCHEDULED", 0, 160)));

        mvc.perform(get("/"))
           .andExpect(status().isOk())
           .andExpect(view().name("index"))
           .andExpect(model().attributeExists("flights"))
           .andExpect(content().string(org.hamcrest.Matchers.containsString("UA1201")));
    }

    @Test
    void successfulLoginStoresSessionAndRedirects() throws Exception {
        when(api.login(eq("dispatcher"), eq("dispatch123")))
                .thenReturn(new AuthResult("tok-123", "dispatcher", "DISPATCHER", 3600000));

        mvc.perform(post("/login")
                        .param("username", "dispatcher")
                        .param("password", "dispatch123"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/"))
           .andExpect(request().sessionAttribute("jwt", "tok-123"));
    }

    @Test
    void failedLoginRedirectsBackWithError() throws Exception {
        when(api.login(any(), any())).thenThrow(new ApiException("Invalid username or password"));

        mvc.perform(post("/login")
                        .param("username", "bad")
                        .param("password", "creds"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/login"))
           .andExpect(flash().attributeExists("error"));
    }

    @Test
    void bookingWithoutSessionRedirectsToLogin() throws Exception {
        mvc.perform(get("/book/1"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/login"));
    }
}
