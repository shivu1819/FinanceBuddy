package com.financebuddy.backend.repository;

import com.financebuddy.backend.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByUserId(Long userId);

    List<BankAccount> findByUserIdAndActiveTrue(Long userId);

    List<BankAccount> findByUserIdAndAccountType(Long userId, String accountType);

    Optional<BankAccount> findByIdAndUserId(Long id, Long userId);
}
