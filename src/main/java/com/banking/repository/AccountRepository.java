package com.banking.repository;

import com.banking.model.Account;
import com.banking.model.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByUserId(Long userId);
    List<Account> findByStatus(AccountStatus status);
    boolean existsByAccountNumber(String accountNumber);
}