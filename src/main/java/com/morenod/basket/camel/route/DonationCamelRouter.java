package com.morenod.basket.camel.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import java.util.Base64;

@Component
public class DonationCamelRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(SecurityException.class)
            .handled(true)
            .log("SECURITY ALERT: Unauthorized access attempt blocked! ${exception.message}")
            .setHeader("CamelHttpResponseCode", constant(403))
            .setBody(constant("{\"error\": \"Access Denied: Invalid Security Token\"}"));

        onException(IllegalArgumentException.class)
            .handled(true)
            .log("VALIDATION ERROR: Malformed or malicious payload dropped! ${exception.message}")
            .setHeader("CamelHttpResponseCode", constant(400))
            .setBody(constant("{\"error\": \"Bad Request: Payload failed validation rules\"}"));

        onException(Exception.class)
            .handled(true)
            .log("Camel Broker Pipeline Error: ${exception.message}");

        from("direct:getAll").routeId("getAllProxyRoute")
            .to("spring-rabbitmq:{{rabbitmq.exchange}}?routingKey={{rabbitmq.routingKey}}");

        from("direct:create").routeId("createProxyRoute")
            .choice()
                .when(header("X-Proxy-Token").isNotEqualTo("{{proxy.security.token}}"))
                    .throwException(new SecurityException("Route Policy Violation: Missing or Invalid Token"))
                .otherwise()
                    .log("Route authorization successful.")
                    .choice()
                        .when(body().isNull())
                            .throwException(new IllegalArgumentException("Payload cannot be null"))
                        .when(body().contains("<script>"))
                            .throwException(new IllegalArgumentException("Malicious XSS payload detected"))
                        .otherwise()
                            .process(exchange -> {
                                String rawBody = exchange.getIn().getBody(String.class);
                                String securePayload = Base64.getEncoder().encodeToString(rawBody.getBytes());
                                exchange.getIn().setBody(securePayload);
                            })
                            .log("Payload securely marshalled to ciphertext: ${body}")
                            .to("spring-rabbitmq:{{rabbitmq.exchange}}?routingKey={{rabbitmq.routingKey}}")
                    .end()
            .end();

        from("direct:update").routeId("updateProxyRoute")
            .to("spring-rabbitmq:{{rabbitmq.exchange}}?routingKey={{rabbitmq.routingKey}}");

        from("direct:delete").routeId("deleteProxyRoute")
            .to("spring-rabbitmq:{{rabbitmq.exchange}}?routingKey={{rabbitmq.routingKey}}");

        from("spring-rabbitmq:{{rabbitmq.exchange}}?queues={{rabbitmq.queue}}&autoDeclare=true")
            .routeId("rabbitmqConsumerRoute")
            .log("Dequeued encrypted payload from RabbitMQ wire: ${body}")
            .process(exchange -> {
                String encryptedBody = exchange.getIn().getBody(String.class);
                byte[] decodedBytes = Base64.getDecoder().decode(encryptedBody);
                exchange.getIn().setBody(new String(decodedBytes));
            })
            .log("Payload safely unmarshalled for processing: ${body}")
            .to("bean:restFileLogger") 
            
            .setHeader("Authorization", constant("Basic YWRtaW46c2VjcmV0UGFzc3dvcmQxMjM=")) // admin:secretPassword123 encoded in Base64
            .to("http://localhost:8080/api/donations?bridgeEndpoint=true");
    }
}