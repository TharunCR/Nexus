package com.banking.controller;

import com.banking.dto.AccountResponse;
import com.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private AccountService accountService;
    
    @GetMapping("/accounts")
    @Hidden
    public ResponseEntity<?> getAllAccounts() {
        try {
            List<AccountResponse> accounts = accountService.getAllAccounts();
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}