package com.morenod.basket;

import com.morenod.basket.dto.DonationRequest;
import com.morenod.basket.model.Donation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelAndDtoTests {

    @Test
    void testDonationModel() {
        Donation d = new Donation();
        d.setId(1L);
        d.setItemName("Test");
        d.setCategory("Pizza");
        d.setQuantity(10);
        d.setDateEntered("26.06.28");
        d.setStatus("PENDING");

        assertEquals(1L, d.getId());
        assertEquals("Test", d.getItemName());
        assertEquals("Pizza", d.getCategory());
        assertEquals(10, d.getQuantity());
        assertEquals("26.06.28", d.getDateEntered());
        assertEquals("PENDING", d.getStatus());
    }

    @Test
    void testDonationRequestDto() {
        DonationRequest dr = new DonationRequest("Item", "Pizza", 1, "Date", "Status");
        assertEquals("Item", dr.getItemName());
        assertEquals("Pizza", dr.getCategory());
        assertEquals(1, dr.getQuantity());
        assertEquals("Date", dr.getDateEntered());
        assertEquals("Status", dr.getStatus());
    }
}