package com.morenod.basket;

import com.morenod.basket.controller.AuthenticationController;
import com.morenod.basket.model.Donation;
import com.morenod.basket.model.User;
import com.morenod.basket.repository.UserRepository;
import com.morenod.basket.repository.DonationRepository;
import com.morenod.basket.fx.DashboardController;
import com.morenod.basket.security.JwtUtil;
import com.morenod.basket.security.SecurityUtil;
import com.morenod.basket.security.SerializeUtil;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityUnitTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private DashboardController dashboardController;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationController authController;

    private final SecurityUtil securityUtil = new SecurityUtil();

    User user = new User(null, "admin", "$2a$10$hashed", "ADMIN");

    @Test
    void testLoginWithValidCredentials() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("admin", "ADMIN")).thenReturn("mock-token");
        when(jwtUtil.generateRefreshToken("admin")).thenReturn("mock-refresh-token");

        ResponseEntity<String> response = authController.login(Map.of("username", "admin","password", "pass"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("mock-token", response.getBody());

        verify(userRepository).findByUsername("admin");
        verify(passwordEncoder).matches("pass", user.getPassword());
        verify(jwtUtil).generateToken("admin", "ADMIN");
        verify(jwtUtil).generateRefreshToken("admin");
        verifyNoMoreInteractions(jwtUtil);
    }

    @Test
    void testLoginWithInvalidPassword() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", user.getPassword())).thenReturn(false);

        ResponseEntity<String> response = authController.login(Map.of("username", "admin","password", "wrong_password"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        verify(userRepository).findByUsername("admin");
        verify(passwordEncoder).matches("wrong_password", user.getPassword());
        verify(jwtUtil, never()).generateToken(any(), any());
        verify(jwtUtil, never()).generateRefreshToken(any());
    }

    @Test
    void testAccessDeniedForUnknownUser() {
        when(userRepository.findByUsername("morenod")).thenReturn(Optional.empty());

        ResponseEntity<String> response = authController.login(Map.of( "username", "morenod","password", "pass"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        verify(userRepository).findByUsername("morenod");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateToken(any(), any());
        verify(jwtUtil, never()).generateRefreshToken(any());
    }

    @Test
    void testIsUrlSafeAllPaths() {
        assertTrue(securityUtil.isUrlSafe("https://api.basket.com/data"));
        assertFalse(securityUtil.isUrlSafe("https://im_a_hacker.com"));
        assertFalse(securityUtil.isUrlSafe("http://127.0.0.1"));
        assertFalse(securityUtil.isUrlSafe("not-a-url"));
        assertFalse(securityUtil.isUrlSafe(""));
    }

    @Test
    void testSerializeUtilInvalidHeader() {
        byte[] maliciousData = new byte[]{0x00, 0x01, 0x02, 0x03}; 
        assertThrows(SecurityException.class, () -> {
            SerializeUtil.secureDeserialize(maliciousData);
        });
    }

    @Test
    void testSerializeUtilSerializationCycle() throws Exception {
        Donation donation = new Donation();
        donation.setId(1L);
        String tempFile = "temp_donation.ser";

        assertDoesNotThrow(() -> SerializeUtil.serializeDonation(donation, tempFile));
        new java.io.File(tempFile).delete();
    }
    
}