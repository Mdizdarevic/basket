package com.morenod.basket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.morenod.basket.security.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTests {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void testTokenLifecycle() {
        String username = "testUser";
        String role = "ADMIN";

        // generate actual tokens
        String accessToken = jwtUtil.generateToken(username, role);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        // validate
        assertTrue(jwtUtil.validateToken(accessToken));
        assertTrue(jwtUtil.validateToken(refreshToken));

        assertEquals(username, jwtUtil.getUsernameFromToken(accessToken));
        assertEquals(username, jwtUtil.getUsernameFromToken(refreshToken));
    }

    @Test
    void testValidateTokenInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.not.a.token";
        assertFalse(jwtUtil.validateToken(invalidToken));
    }
    
    @Test
    void testValidateTokenEmptyToken() {
        assertFalse(jwtUtil.validateToken(""));
    }
}