package com.smartbank.account.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public class CreateAccountRequest {
    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 100)
    private String ownerName;
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 letters (e.g., INR, USD)")
    private String currency;
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}