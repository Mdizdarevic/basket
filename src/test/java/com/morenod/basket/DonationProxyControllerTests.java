package com.morenod.basket;

import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.morenod.basket.controller.DonationProxyController;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationProxyControllerTest {

    @Mock private ProducerTemplate producerTemplate;
    @InjectMocks private DonationProxyController controller;

    @Test
    void testProxyAccessDeniedVerifiesHeadersAndBody() {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/proxy/donations");
        
        // Mock Camel to return "Access Denied"
        when(producerTemplate.requestBodyAndHeaders(eq("direct:create"), any(), anyMap()))
            .thenReturn("Access Denied");

        ResponseEntity<String> response = controller.proxyPutDonation("invalid-token", "{}", request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("text/plain; charset=utf-8", response.getHeaders().getFirst("Content-Type"));
        assertEquals("Access Denied: The request was rejected.", response.getBody());
    }

    @Test
    void testProxyGetAccessDeniedVerifiesAllBranches() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/proxy/donations");
        when(producerTemplate.requestBodyAndHeaders(eq("direct:create"), eq(""), anyMap()))
            .thenReturn("Access Denied");

        ResponseEntity<String> response = controller.proxyGetDonation("token", request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().contains("Access Denied"));
    }

    @Test
    void testProxyDeleteAccessDeniedVerifiesAllBranches() {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/proxy/donations");
        when(producerTemplate.requestBodyAndHeaders(eq("direct:create"), anyString(), anyMap()))
            .thenReturn("Access Denied");

        ResponseEntity<String> response = controller.proxyDeleteDonation("token", null, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access Denied: The request was rejected.", response.getBody());
    }
}