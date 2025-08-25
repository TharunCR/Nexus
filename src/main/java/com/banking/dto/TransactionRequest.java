package com.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransactionRequest {
    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    private String description;
    
    // For transfer operations
    private String toAccountNumber;
    
    public TransactionRequest() {}
    
    public TransactionRequest(BigDecimal amount, String description) {
        this.amount = amount;
        this.description = description;
    }
    
    public TransactionRequest(BigDecimal amount, String description, String toAccountNumber) {
        this.amount = amount;
        this.description = description;
        this.toAccountNumber = toAccountNumber;
    }
    
    // Getters and Setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getToAccountNumber() { return toAccountNumber; }
    public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }
}