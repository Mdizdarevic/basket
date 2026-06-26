package com.morenod.basket.camel.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component("restFileLogger")
public class RestFileLogger implements Processor {

    private static final String LOG_DIR = "logs/api_payloads/";

    @Override
public void process(Exchange exchange) throws Exception {
    // create directory if it doesn't exist
    File dir = new File(LOG_DIR);
    if (!dir.exists()) {
        dir.mkdirs();
    }

    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
    String httpMethod = exchange.getIn().getHeader("httpMethod", String.class);    
    String routeId = exchange.getFromRouteId();
    
    String fileName = String.format("%s_%s_%s.json", timestamp, httpMethod, routeId);
    File logFile = new File(dir, fileName);

    String body = exchange.getIn().getBody(String.class);
    String logContent;

    if (body == null || body.trim().isEmpty()) {
        logContent = String.format(
            "{\n  \"routeId\": \"%s\",\n  \"httpMethod\": \"%s\",\n  \"info\": \"Empty body or No Content response\"\n}",
            routeId, httpMethod
        );
    } else {
        body = body.trim();
        if (body.startsWith("{")) {
            String metadata = String.format("\n  \"routeId\": \"%s\",\n  \"httpMethod\": \"%s\",\n  ", routeId, httpMethod);
            logContent = "{" + metadata + body.substring(1);
        } else {
            logContent = String.format(
                "{\n  \"routeId\": \"%s\",\n  \"httpMethod\": \"%s\",\n  \"payload\": %s\n}",
                routeId, httpMethod, body
            );
        }
    }

    Files.writeString(logFile.toPath(), logContent, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
}
}