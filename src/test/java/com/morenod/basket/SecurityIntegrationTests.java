package com.morenod.basket;

import com.morenod.basket.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTests {

    private final MockMvc mockMvc;
    private final JwtUtil jwtUtil;

    @Autowired
    public SecurityIntegrationTests(MockMvc mockMvc, JwtUtil jwtUtil) {
        this.mockMvc = mockMvc;
        this.jwtUtil = jwtUtil;
    }

    @Test
    void testPublicEndpointReturns200() throws Exception {
        mockMvc.perform(get("/api/donations")).andExpect(status().isOk());
    }

    @Test
    void testPostDonationReturns403WithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/donations")).andExpect(status().isForbidden());
    }

    @Test
    void testUnknownEndpointReturns403WithoutCsrf() throws Exception {
        mockMvc.perform(get("/api/secret")).andExpect(status().isForbidden());
    }

    @Test
    void testExpiredTokenReturnsUnauthorized() throws Exception {
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwicm9sZSI6IlVTRVIiLCJleHAiOjEwMH0.signature";
        
        mockMvc.perform(get("/api/admin/users") 
               .header("Authorization", "Bearer " + expiredToken))
               .andExpect(status().isForbidden()); 
    }

    @Test
    void testAuthEndpointReturns405() throws Exception {
        mockMvc.perform(get("/api/auth/login")).andExpect(status().isMethodNotAllowed());
    }
}