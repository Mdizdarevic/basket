package com.morenod.basket;

import com.morenod.basket.camel.processor.RestFileLogger;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class RestFileLoggerTests {

    @TempDir
    Path tempDir;

    @Test
    void testProcessWritesJsonFile() throws Exception {
        RestFileLogger logger = new RestFileLogger();
        DefaultExchange exchange = new DefaultExchange(new DefaultCamelContext());
        
        exchange.getIn().setHeader("httpMethod", "POST");
        exchange.getIn().setHeader("CamelFromRouteId", "testRoute");
        exchange.getIn().setBody("{\"test\":\"data\"}");

        assertDoesNotThrow(() -> logger.process(exchange));
        
        assertTrue(tempDir.toFile().exists(), "Temp directory should exist");
        File[] files = tempDir.toFile().listFiles();
        assertNotNull(files, "Files should be created");
    }
}