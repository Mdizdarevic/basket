package com.morenod.basket.controller;

import com.morenod.basket.model.Donation;
import com.morenod.basket.repository.DonationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private final DonationRepository donationRepository;

    public DonationController(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    // 1. GET ALL
    @GetMapping
    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    // 2. GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Donation> getDonationById(@PathVariable Long id) {
        return donationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. POST (CREATE)
    @PostMapping
    public Donation createDonation(@RequestBody Donation donation) {
        return donationRepository.save(donation);
    }

    // 4. PUT (UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<Donation> updateDonation(@PathVariable Long id, @RequestBody Donation updatedDonation) {
        return donationRepository.findById(id)
                .map(donation -> {
                    donation.setItemName(updatedDonation.getItemName());
                    donation.setCategory(updatedDonation.getCategory());
                    donation.setQuantity(updatedDonation.getQuantity());
                    donation.setDateEntered(updatedDonation.getDateEntered());
                    donation.setStatus(updatedDonation.getStatus());
                    return ResponseEntity.ok(donationRepository.save(donation));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonation(@PathVariable Long id) {
        if (donationRepository.existsById(id)) {
            donationRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

}