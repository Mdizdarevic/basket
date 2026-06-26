package com.morenod.basket.config;

import com.morenod.basket.repository.DonationRepository;
import com.morenod.basket.repository.UserRepository; 
import com.morenod.basket.model.Donation;
import com.morenod.basket.model.User;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; 

@Configuration 
public class DataInitializer {
    // set up defualt data for the database just to test things
    @Bean
    public CommandLineRunner initData(UserRepository userRepo, DonationRepository donationRepo) {
        return args -> {
            userRepo.save(new User(null, "admin", "admin123", "ADMIN"));
            userRepo.save(new User(null, "donor_bakery", "pass123", "DONOR"));
            
            donationRepo.save(new Donation(null, "Organic Whole Wheat Bread", "Bakery Surplus", 30, "12.06.2026", "PENDING"));
            donationRepo.save(new Donation(null, "Fresh Apples", "Produce", 100, "18.06.2026", "APPROVED"));
            donationRepo.save(new Donation(null, "Canned Tomato Soup", "Pantry", 45, "01.12.2027", "DISTRIBUTED"));
                    };
    }
}