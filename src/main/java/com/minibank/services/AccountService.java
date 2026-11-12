package com.minibank.services;

import org.springframework.stereotype.Service;
import com.minibank.repositories.AccountRepository;
import com.minibank.entities.AccountEntity;
import com.minibank.exceptions.AccountNotFoundException;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import java.math.RoundingMode;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountEntity createAccount(String ownerName) {
        AccountEntity account = new AccountEntity();
        account.setOwnerName(ownerName);
        account.setAccountNumber(UUID.randomUUID().toString());
        account.setBalance(BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_EVEN));
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public AccountEntity getAccount(Long id) {
        return accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    @Transactional(readOnly = true)
    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAll();
    }
}