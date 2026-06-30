package com.morenod.basket.config;

import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class CamelTrafficInterceptor implements Filter {

    // constructor instead of autowired
    private final ProducerTemplate producerTemplate;

    public CamelTrafficInterceptor(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        chain.doFilter(request, response);

        if (path.startsWith("/api/donations")) {
            String routeDirect = "direct:getAll";
            if ("POST".equalsIgnoreCase(method)) routeDirect = "direct:create";
            if ("PUT".equalsIgnoreCase(method)) routeDirect = "direct:update";
            if ("DELETE".equalsIgnoreCase(method)) routeDirect = "direct:delete";

            String payload = "{\"info\": \"Action: " + method + " executed on path " + path + "\"}";

            try {
                producerTemplate.sendBodyAndHeader(routeDirect, payload, "CamelHttpMethod", method);
            } catch (Exception e) {
                System.err.println("Camel Interceptor Warning: " + e.getMessage());
            }
        }
    }
}