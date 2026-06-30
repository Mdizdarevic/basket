package com.morenod.basket;

import com.morenod.basket.controller.DonationController;
import com.morenod.basket.dto.DonationRequest;
import com.morenod.basket.model.Donation;
import com.morenod.basket.repository.DonationRepository;
import com.morenod.basket.security.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InvalidClassException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationControllerUnitTests {

    @Mock private DonationRepository donationRepository;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private SecurityUtil securityUtil;

    @InjectMocks
    private DonationController donationController;

    @Test
    void getAllDonationsReturnsList() {
        when(donationRepository.findAll()).thenReturn(List.of(new Donation()));
        List<Donation> result = donationController.getAllDonations();
        assertFalse(result.isEmpty());
        verify(donationRepository).findAll();
    }

    @Test
    void getDonationById_ExistsReturnsOk() {
        when(donationRepository.findById(1L)).thenReturn(Optional.of(new Donation()));
        ResponseEntity<Donation> response = donationController.getDonationById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getDonationById_NotFoundReturns404() {
        when(donationRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseEntity<Donation> response = donationController.getDonationById(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createDonationSavesAndReturns() {
        DonationRequest req = new DonationRequest("Pizza", "Food", 5, "2026-06-28", "PENDING");
        when(donationRepository.save(any(Donation.class))).thenAnswer(i -> i.getArguments()[0]);
        
        Donation result = donationController.createDonation(req);
        assertEquals("Pizza", result.getItemName());
        verify(donationRepository).save(any(Donation.class));
    }

    @Test
    void deleteDonationExistsReturnsOk() {
        when(donationRepository.existsById(1L)).thenReturn(true);
        ResponseEntity<Void> response = donationController.deleteDonation(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(donationRepository).deleteById(1L);
    }

    @Test
    void deleteDonationNotFoundReturns404() {
        when(donationRepository.existsById(1L)).thenReturn(false);
        ResponseEntity<Void> response = donationController.deleteDonation(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void secureFetchBlocksUnsafeUrlThrowsException() {
        when(securityUtil.isUrlSafe("http://bad-url.com")).thenReturn(false);
        assertThrows(SecurityException.class, () -> donationController.secureFetch("http://bad-url.com"));
    }

    @Test
    void secureFetchAllowsSafeUrlReturnsSuccess() {
        when(securityUtil.isUrlSafe("http://good-url.com")).thenReturn(true);
        String response = donationController.secureFetch("http://good-url.com");
        assertTrue(response.contains("Fetch successful"));
    }

    @Test
    void testSerializationTrigger() {
        assertDoesNotThrow(() -> donationController.serializeTest());
    }

    @Test
    void updateDonationValidReturnsUpdated() {
        DonationRequest req = new DonationRequest("Updated Pizza", "Food", 10, "2026-06-29", "DONE");
        when(donationRepository.findById(1L)).thenReturn(Optional.of(new Donation()));
        when(donationRepository.save(any(Donation.class))).thenAnswer(i -> i.getArguments()[0]);

        ResponseEntity<Donation> response = donationController.updateDonation(1L, req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(donationRepository).save(any(Donation.class));
    }

    @Test
    void searchVulnerableExecutes() {
        assertDoesNotThrow(() -> donationController.searchVulnerable("test_input"));
    }

    @Test
    void deserializeFailShouldThrowSecurityException() {
        assertThrows(InvalidClassException.class, () -> {
            donationController.deserializeFail();
        });
    }

    @Test
    void testInvalidBinaryRejectsNonSerializedFile() {
        assertDoesNotThrow(() -> {
            String result = donationController.testInvalidBinary();
            assertTrue(result.contains("Rejected:"));
        });
    }

    @Test
    void donationRequestSetterCoverage() {
        DonationRequest req = new DonationRequest();
        
        req.setItemName("Test Name");
        req.setCategory("TestCat");
        req.setQuantity(1);
        req.setDateEntered("26.01.26");
        req.setStatus("OK");
        
        assertEquals("Test Name", req.getItemName());
        assertEquals("TestCat", req.getCategory());
        assertEquals(1, req.getQuantity());
        assertEquals("26.01.26", req.getDateEntered());
        assertEquals("OK", req.getStatus());
    }

    @Test
    void donationRequestNoArgsConstructor() {
        DonationRequest req = new DonationRequest();
        assertNotNull(req);
        assertNull(req.getItemName());
    }
}