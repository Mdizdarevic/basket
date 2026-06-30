package com.morenod.basket.dto;

// data transfer object made to carry donation reuest data
public class DonationRequest {
    private String itemName;
    private String category;
    private Integer quantity;
    private String dateEntered;
    private String status;

    public DonationRequest() {
    }

    public DonationRequest(String itemName, String category, Integer quantity, String dateEntered, String status) {
        this.itemName = itemName;
        this.category = category;
        this.quantity = quantity;
        this.dateEntered = dateEntered;
        this.status = status;
    }

    // Getters and Setters
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