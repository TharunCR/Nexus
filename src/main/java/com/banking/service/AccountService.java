package com.banking.service;

import com.banking.dto.AccountResponse;
import com.banking.dto.TransactionRequest;
import com.banking.dto.TransactionResponse;
import com.banking.model.*;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public AccountResponse getAccountDetails(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));
        
        // Check if current user can access this account
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        if (!currentUser.getRole().equals(Role.ADMIN) && !account.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You can only access your own account");
        }
        
        return mapToAccountResponse(account);
    }
    
    public AccountResponse getMyAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        Account account = accountRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Account not found for user"));
        
        return mapToAccountResponse(account);
    }
    
    public BigDecimal getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));
        
        // Check if current user can access this account
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        if (!currentUser.getRole().equals(Role.ADMIN) && !account.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You can only access your own account");
        }
        
        return account.getBalance();
    }
    
    @Transactional
    public TransactionResponse deposit(String accountNumber, TransactionRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));
        
        // Check if current user can access this account
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        if (!currentUser.getRole().equals(Role.ADMIN) && !account.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You can only access your own account");
        }
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }
        
        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);
        
        Transaction transaction = new Transaction(
                request.getAmount(),
                TransactionType.DEPOSIT,
                request.getDescription() != null ? request.getDescription() : "Deposit",
                account,
                newBalance
        );
        
        transactionRepository.save(transaction);
        
        return mapToTransactionResponse(transaction);
    }
    
    @Transactional
    public TransactionResponse withdraw(String accountNumber, TransactionRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));
        
        // Check if current user can access this account
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        if (!currentUser.getRole().equals(Role.ADMIN) && !account.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You can only access your own account");
        }
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }
        
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        
        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);
        
        Transaction transaction = new Transaction(
                request.getAmount(),
                TransactionType.WITHDRAWAL,
                request.getDescription() != null ? request.getDescription() : "Withdrawal",
                account,
                newBalance
        );
        
        transactionRepository.save(transaction);
        
        return mapToTransactionResponse(transaction);
    }
    
    @Transactional
    public String transfer(String fromAccountNumber, TransactionRequest request) {
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("Source account not found"));
        
        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new RuntimeException("Destination account not found"));
        
        // Check if current user can access the source account
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        if (!currentUser.getRole().equals(Role.ADMIN) && !fromAccount.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You can only transfer from your own account");
        }
        
        if (fromAccount.getStatus() != AccountStatus.ACTIVE || toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("One or both accounts are not active");
        }
        
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        
        if (fromAccount.getAccountNumber().equals(toAccount.getAccountNumber())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }
        
        // Debit from source account
        BigDecimal newFromBalance = fromAccount.getBalance().subtract(request.getAmount());
        fromAccount.setBalance(newFromBalance);
        accountRepository.save(fromAccount);
        
        // Credit to destination account
        BigDecimal newToBalance = toAccount.getBalance().add(request.getAmount());
        toAccount.setBalance(newToBalance);
        accountRepository.save(toAccount);
        
        // Create transaction records
        Transaction debitTransaction = new Transaction(
                request.getAmount(),
                TransactionType.TRANSFER_OUT,
                "Transfer to " + toAccount.getAccountNumber(),
                fromAccount,
                newFromBalance
        );
        debitTransaction.setToAccountNumber(toAccount.getAccountNumber());
        transactionRepository.save(debitTransaction);
        
        Transaction creditTransaction = new Transaction(
                request.getAmount(),
                TransactionType.TRANSFER_IN,
                "Transfer from " + fromAccount.getAccountNumber(),
                toAccount,
                newToBalance
        );
        creditTransaction.setFromAccountNumber(fromAccount.getAccountNumber());
        transactionRepository.save(creditTransaction);
        
        return "Transfer completed successfully";
    }
    
    public List<TransactionResponse> getTransactionHistory(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));
        
        // Check if current user can access this account
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        if (!currentUser.getRole().equals(Role.ADMIN) && !account.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You can only access your own account transactions");
        }
        
        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByTransactionDateDesc(account.getId());
        
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccountResponse> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }
    
    private AccountResponse mapToAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getAccountType(),
                account.getStatus(),
                account.getUser().getFirstName() + " " + account.getUser().getLastName(),
                account.getCreatedAt()
        );
    }
    
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getBalanceAfter(),
                transaction.getToAccountNumber(),
                transaction.getFromAccountNumber()
        );
    }
}