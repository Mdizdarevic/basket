package com.morenod.basket.camel.route;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import java.util.Base64;

@Component
public class DonationCamelRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // exceptions and error checking
        onException(SecurityException.class)
            .handled(true)
            .log("SECURITY ALERT: Unauthorized access blocked! ${exception.message}")
            .setHeader("CamelHttpResponseCode", constant(403))
            .setBody(constant("{\"error\": \"Access Denied: Invalid Security Token\"}"));

        onException(IllegalArgumentException.class)
            .handled(true) 
            .log("VALIDATION ERROR: Illegal arugment! ${exception.message}")
            .setHeader("CamelHttpResponseCode", constant(400))
            .setBody(constant("{\"error\": \"Bad Request: Payload failed validation rules\"}"));

        onException(Exception.class)
            .handled(true)
            .log("Error: ${exception.message}");

        // rroutes 
        from("direct:getAll").routeId("getAllProxyRoute")
            .to("spring-rabbitmq:{{rabbitmq.exchange}}?routingKey={{rabbitmq.routingKey}}");

        from("direct:create").routeId("createProxyRoute")
            .choice()
                .when(header("X-Proxy-Token").isNotEqualTo("SecureBasketToken2026"))                    
                .throwException(new SecurityException("ROUTE ERROR: Missing or Invalid Token"))
                .otherwise()
                    .log("Route authorization successful.")
                    .choice()
                        .when(body().isNull())
                            .throwException(new IllegalArgumentException("Payload cannot be null"))
                        .when(body().contains("<script>"))
                            .throwException(new IllegalArgumentException("Malicious XSS payload detected"))
                        .otherwise()
                            .process(exchange -> { // converting raw data to secure base64
                                String rawBody = exchange.getIn().getBody(String.class);
                                String securePayload = Base64.getEncoder().encodeToString(rawBody.getBytes());
                                exchange.getIn().setBody(securePayload);
                            })
                            .log("Payload securely went through: ${body}")
                            .to("spring-rabbitmq:{{rabbitmq.exchange}}?routingKey={{rabbitmq.routingKey}}")
                    .end()
            .end();

        from("direct:update").routeId("updateProxyRoute")
            .to("spring-rabbitmq:{{rabbitmq.exchange}}?routingKey={{rabbitmq.routingKey}}");

        from("direct:delete").routeId("deleteProxyRoute")
            .to("spring-rabbitmq:{{rabbitmq.exchange}}?routingKey={{rabbitmq.routingKey}}");

        // from("spring-rabbitmq:{{rabbitmq.exchange}}?queues={{rabbitmq.queue}}&autoDeclare=true")
        from("spring-rabbitmq:{{rabbitmq.exchange}}?queues={{rabbitmq.queue}}&routingKey={{rabbitmq.routingKey}}&autoDeclare=true")
            .routeId("rabbitmqConsumerRoute")
            .log("Payload received from RabbitMQ: ${body}")
            .process(exchange -> { // converting raw data to secure base64
                String encryptedBody = exchange.getIn().getBody(String.class);
                byte[] decodedBytes = Base64.getDecoder().decode(encryptedBody);
                exchange.getIn().setBody(new String(decodedBytes));
            })
            .log("Payload securely went through: ${body}")
            
            .setHeader("httpMethod", header("httpMethod"))
            .to("bean:restFileLogger")
            
            .to("direct:saveToDatabase");

        from("direct:saveToDatabase")
            .routeId("saveToDatabaseRoute")
            .process(exchange -> {
                String rawJson = exchange.getIn().getBody(String.class);
                
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.morenod.basket.model.Donation donation = mapper.readValue(rawJson, com.morenod.basket.model.Donation.class);
                
                exchange.getIn().setBody(donation);
            })
            .to("bean:donationRepository?method=save");        
        }
}