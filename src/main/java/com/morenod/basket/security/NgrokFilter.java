package com.morenod.basket.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import java.io.IOException;

// chechking if request is comging thru ngork tunnel
@Component
@Order(1) // i need to run ngork before the other filters
public class NgrokFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        String host = httpRequest.getHeader("Host");
        
        if (host != null && host.contains("ngrok-free.dev")) {
            chain.doFilter(request, response);
            return;
        }
        
        chain.doFilter(request, response);
    }
}