package com.banking.config;

import com.banking.model.*;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", "admin@banking.com", 
                    passwordEncoder.encode("admin123"), "Admin", "User", Role.ADMIN);
            userRepository.save(admin);
            
            Account adminAccount = new Account("1000000001", AccountType.BUSINESS, admin);
            adminAccount.setBalance(new BigDecimal("1000000.00"));
            accountRepository.save(adminAccount);
            
            System.out.println("Admin user created: username=admin, password=admin123, account=1000000001");
        }
        
        // Create sample customer if not exists
        if (!userRepository.existsByUsername("customer1")) {
            User customer = new User("customer1", "customer1@banking.com", 
                    passwordEncoder.encode("customer123"), "John", "Doe", Role.CUSTOMER);
            userRepository.save(customer);
            
            Account customerAccount = new Account("2000000001", AccountType.SAVINGS, customer);
            customerAccount.setBalance(new BigDecimal("5000.00"));
            accountRepository.save(customerAccount);
            
            System.out.println("Sample customer created: username=customer1, password=customer123, account=2000000001");
        }
        
        // Create another sample customer if not exists
        if (!userRepository.existsByUsername("customer2")) {
            User customer2 = new User("customer2", "customer2@banking.com", 
                    passwordEncoder.encode("customer123"), "Jane", "Smith", Role.CUSTOMER);
            userRepository.save(customer2);
            
            Account customer2Account = new Account("2000000002", AccountType.CHECKING, customer2);
            customer2Account.setBalance(new BigDecimal("3000.00"));
            accountRepository.save(customer2Account);
            
            System.out.println("Sample customer2 created: username=customer2, password=customer123, account=2000000002");
        }
    }
}