package com.morenod.basket.controller;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/proxy")
public class DonationProxyController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @PostMapping("/donations")
    public ResponseEntity<String> proxyDonation(
            @RequestHeader(value = "X-Proxy-Token", required = false) String proxyToken,
            @RequestBody String jsonPayload) {
        
        Map<String, Object> headers = Map.of("X-Proxy-Token", proxyToken != null ? proxyToken : "");
        
        Object response = producerTemplate.requestBodyAndHeaders("direct:create", jsonPayload, headers);
        
        if (response != null && response.toString().contains("Access Denied")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response.toString());
        }
        
        return ResponseEntity.ok("{\"status\": \"Proxied via Secured Camel Route & RabbitMQ\"}");
    }
}