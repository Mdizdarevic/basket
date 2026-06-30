package com.morenod.basket.controller;
import com.morenod.basket.dto.DonationRequest; 
import com.morenod.basket.model.Donation;
import com.morenod.basket.repository.DonationRepository;
import com.morenod.basket.security.SecurityUtil;
import com.morenod.basket.security.SerializeUtil;
import com.morenod.basket.security.WhitelistObjectInputStream;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private final DonationRepository donationRepository;
    private final JdbcTemplate jdbcTemplate; 
    private final SecurityUtil securityUtil; // needs securityutil

    public DonationController(DonationRepository donationRepository, JdbcTemplate jdbcTemplate, SecurityUtil securityUtil) {
        this.donationRepository = donationRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.securityUtil = securityUtil;
    }

    // GET ALL
    @GetMapping
    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Donation> getDonationById(@PathVariable Long id) {
        return donationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST (CREATE) 
    @PostMapping // with request now
    public Donation createDonation(@RequestBody DonationRequest request) {
        Donation donation = new Donation();
        donation.setItemName(request.getItemName());
        donation.setCategory(request.getCategory());
        donation.setQuantity(request.getQuantity());
        donation.setDateEntered(request.getDateEntered());
        donation.setStatus(request.getStatus());
        return donationRepository.save(donation);
    }

    // PUT (UPDATE)
    @PutMapping("/{id}") // also with request now
    public ResponseEntity<Donation> updateDonation(@PathVariable Long id, @RequestBody DonationRequest request) {
        return donationRepository.findById(id)
                .map(donation -> {
                    donation.setItemName(request.getItemName());
                    donation.setCategory(request.getCategory());
                    donation.setQuantity(request.getQuantity());
                    donation.setDateEntered(request.getDateEntered());
                    donation.setStatus(request.getStatus());
                    return ResponseEntity.ok(donationRepository.save(donation));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonation(@PathVariable Long id) {
        if (donationRepository.existsById(id)) {
            donationRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    // search logic actually implemented
    @GetMapping("/search")
    public List<Donation> searchDonations(@RequestParam(value = "q", defaultValue = "") String query) {
        return donationRepository.findAll()
            .stream()
            .filter(d -> d.getItemName().contains(query))
            .toList();
    }

    // search (intentioanlly vulnerable for testing)
    @GetMapping("/search-demo")
    public List<Donation> searchVulnerable(@RequestParam("q") String query) {
        String sql = "SELECT * FROM donations WHERE item_name = '" + query + "'";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Donation.class));
    }

    // made to test fetching allowed url and blcok forbidden urls
    @GetMapping("/fetch-url")
    public String secureFetch(@RequestParam String url) {
        if (!securityUtil.isUrlSafe(url)) {
            throw new SecurityException("Forbidden: URL blocked!");
        }
        return "Fetch successful! URL " + url + " is trusted.";
    }

    // vulnerable fetch, just for testing!
    @GetMapping("/fetch-demo")
    public String vulnerableFetch(@RequestParam String url) {
        return "Vulnerable fetch triggered for: " + url;
    }

    // serialize API call for testing
    @GetMapping("/serialize-test")
    public String serializeTest() throws Exception {
        Donation serDonation = new Donation(1L, "Pizza", "Party food", 5, "26.06.2026", "PENDING");
        SerializeUtil.serializeDonation(serDonation, "demo.ser");

        byte[] fileBytes = Files.readAllBytes(Paths.get("demo.ser"));
        Donation result = (Donation) SerializeUtil.secureDeserialize(fileBytes);

        return "Success! Deserialized object: " + result.getItemName();
    }

    // testing if deserializing a non-whitelisted object fails
    @GetMapping("/deserialize-fail")
    public String deserializeFail() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(new java.util.HashMap<>());
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
             WhitelistObjectInputStream wois = new WhitelistObjectInputStream(bais)) {
            wois.readObject();
        }

        return "Fail! You tried deserilaizing a non-whitelisted object!";
    }

    // error for non-serialized file (i.e text fiel)
    @GetMapping("/non-serialized-file-demo")
    public String testInvalidBinary() throws Exception {
        byte[] nonSerFile = "This is a non-serialized file".getBytes();

        try {
            SerializeUtil.secureDeserialize(nonSerFile);
        } catch (Exception e) {
            return "Rejected: " + e.getMessage();
        }
        
        return "failed to reject non-serialized file!";
    }

}