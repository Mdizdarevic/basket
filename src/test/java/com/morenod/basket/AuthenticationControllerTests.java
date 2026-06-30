package com.morenod.basket;

import com.morenod.basket.controller.AuthenticationController;
import com.morenod.basket.model.User; 
import com.morenod.basket.repository.UserRepository;
import com.morenod.basket.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTests {

    @Mock private UserRepository userRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthenticationController authController;


    @Test
    void loginPasswordMigrationRequired() {
        User user = new User(null, "admin", "normal_password", "ADMIN");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("normal_password")).thenReturn("$2a$10$encoded");

        ResponseEntity<String> response = authController.login(Map.of("username", "admin", "password", "normal_password"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Password migrated"));
        verify(userRepository).save(user);
    }

    @Test
    void loginSuccess() {
        User user = new User(null, "admin", "$2a$10$encoded", "ADMIN");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "$2a$10$encoded")).thenReturn(true);
        when(jwtUtil.generateToken("admin", "ADMIN")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("admin")).thenReturn("refresh-token");

        ResponseEntity<String> response = authController.login(Map.of("username", "admin", "password", "password"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst("Set-Cookie"));
    }

    @Test
    void loginInvalidCredentials() {
        User user = new User(null, "admin", "$2a$10$encoded", "ADMIN");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$10$encoded")).thenReturn(false);

        ResponseEntity<String> response = authController.login(Map.of("username", "admin", "password", "wrong"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void refreshTokenSuccess() {
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("valid-token")).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User(null, "admin", "pass", "ADMIN")));
        when(jwtUtil.generateToken("admin", "ADMIN")).thenReturn("new-token");

        ResponseEntity<?> response = authController.refreshToken(Map.of("refreshToken", "valid-token"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new-token", response.getBody());
    }
}