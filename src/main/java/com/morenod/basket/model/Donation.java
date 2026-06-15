package com.morenod.basket.model;

import jakarta.persistence.*;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String itemName;
    private String category;
    private Integer quantity;
    private String dateEntered;
    private String status;

    public Donation() {
    }

    public Donation(Long id, String itemName, String category, Integer quantity, String dateEntered, String status) {
        this.id = id;
        this.itemName = itemName;
        this.category = category;
        this.quantity = quantity;
        this.dateEntered = dateEntered;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getDateEntered() { return dateEntered; }
    public void setDateEntered(String dateEntered) { this.dateEntered = dateEntered; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}