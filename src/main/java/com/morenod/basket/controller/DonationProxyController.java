package com.morenod.basket.controller;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/proxy")
public class DonationProxyController {

    @Autowired
    private ProducerTemplate producerTemplate;

    //  proxy for PUT
    @PostMapping("/donations")
    public ResponseEntity<String> proxyDonation(
            @RequestHeader(value = "X-Proxy-Token", required = false) String proxyToken,
            @RequestBody String jsonPayload,
            HttpServletRequest request) {
        
        System.out.println("DEBUG RECEIVED POST TOKEN: [" + proxyToken + "]");

        Map<String, Object> headers = Map.of(
            "X-Proxy-Token", proxyToken != null ? proxyToken : "",
            "httpMethod", request.getMethod() 
        );
        
        Object response = producerTemplate.requestBodyAndHeaders("direct:create", jsonPayload, headers);
        
        if (response != null && response.toString().contains("Access Denied")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response.toString());
        }
        
        return ResponseEntity.ok("Status: Access Granted and POST request logged!");
    }

    @PutMapping("/donations")
    public ResponseEntity<String> proxyPutDonation(
            @RequestHeader(value = "X-Proxy-Token", required = false) String proxyToken,
            @RequestBody String jsonPayload,
            HttpServletRequest request) {
        
        System.out.println("DEBUG RECEIVED PUT TOKEN: [" + proxyToken + "]");

        Map<String, Object> headers = Map.of(
            "X-Proxy-Token", proxyToken != null ? proxyToken : "",
            "httpMethod", request.getMethod() 
        );
        
        Object response = producerTemplate.requestBodyAndHeaders("direct:create", jsonPayload, headers);
        
        if (response != null && response.toString().contains("Access Denied")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response.toString());
        }
        
        return ResponseEntity.ok("Status: Access Granted and PUT request logged!");
    }

    @GetMapping("/donations")
    public ResponseEntity<String> proxyGetDonation(
            @RequestHeader(value = "X-Proxy-Token", required = false) String proxyToken,
            HttpServletRequest request) {
        
        System.out.println("DEBUG RECEIVED GET TOKEN: [" + proxyToken + "]");

        Map<String, Object> headers = Map.of(
            "X-Proxy-Token", proxyToken != null ? proxyToken : "",
            "httpMethod", request.getMethod() 
        );
        
        // GET has no body, so we pass empty string into Camel
        Object response = producerTemplate.requestBodyAndHeaders("direct:create", "", headers);
        
        if (response != null && response.toString().contains("Access Denied")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response.toString());
        }
        
        return ResponseEntity.ok("Status: Access Granted and GET request logged!");
    }

    @DeleteMapping("/donations")
    public ResponseEntity<String> proxyDeleteDonation(
            @RequestHeader(value = "X-Proxy-Token", required = false) String proxyToken,
            @RequestBody(required = false) String jsonPayload,
            HttpServletRequest request) {
        
        System.out.println("DEBUG RECEIVED DELETE TOKEN: [" + proxyToken + "]");

        Map<String, Object> headers = Map.of(
            "X-Proxy-Token", proxyToken != null ? proxyToken : "",
            "httpMethod", request.getMethod() 
        );
        
        // checking in case delete payload is empty or contains a target ID JSON
        String payload = jsonPayload != null ? jsonPayload : "";
        Object response = producerTemplate.requestBodyAndHeaders("direct:create", payload, headers);
        
        if (response != null && response.toString().contains("Access Denied")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response.toString());
        }
        
        return ResponseEntity.ok("Status: Access Granted and DELETE request logged!");
    }
}