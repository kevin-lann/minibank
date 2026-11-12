package com.minibank.services;

import com.minibank.entities.AccountEntity;
import com.minibank.entities.TransactionEntity;
import com.minibank.entities.TransactionType;
import com.minibank.exceptions.AccountNotFoundException;
import com.minibank.repositories.AccountRepository;
import com.minibank.repositories.TransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.minibank.exceptions.InsufficientFundsException;

@Service
public class TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionEntity deposit(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientFundsException("Amount must be greater than 0");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Amount must have at most 2 decimal places");
        }
        AccountEntity account = getAccount(accountId);
        BigDecimal balanceAfter = account.getBalance().add(amount);
        account.setBalance(balanceAfter);
        return writeLedgerRow(account, TransactionType.DEPOSIT, amount, balanceAfter);
    }

    @Transactional
    public TransactionEntity withdraw(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientFundsException("Amount must be greater than 0");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Amount must have at most 2 decimal places");
        }

        AccountEntity account = getAccount(accountId);

        if (amount.compareTo(account.getBalance()) > 0) {
            throw new InsufficientFundsException("Amount must be less than the account balance");
        }

        BigDecimal balanceAfter = account.getBalance().subtract(amount);
        account.setBalance(balanceAfter);
        return writeLedgerRow(account, TransactionType.WITHDRAWAL, amount, balanceAfter);
    }

    @Transactional
    public List<TransactionEntity> transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientFundsException("Amount must be greater than 0");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Amount must have at most 2 decimal places");
        }
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("From account and to account cannot be the same");
        }
        AccountEntity fromAccount = getAccount(fromAccountId);
        AccountEntity toAccount = getAccount(toAccountId);

        if (amount.compareTo(fromAccount.getBalance()) > 0) {
            throw new InsufficientFundsException("Amount must be less than the account balance");
        }

        BigDecimal fromBalanceAfter = fromAccount.getBalance().subtract(amount);
        BigDecimal toBalanceAfter = toAccount.getBalance().add(amount);
        fromAccount.setBalance(fromBalanceAfter);
        toAccount.setBalance(toBalanceAfter);

        TransactionEntity transferOut = writeLedgerRow(fromAccount, TransactionType.TRANSFER_OUT, amount, fromBalanceAfter);
        TransactionEntity transferIn = writeLedgerRow(toAccount, TransactionType.TRANSFER_IN, amount, toBalanceAfter);
        return List.of(transferOut, transferIn);
    }

    @Transactional(readOnly = true)
    public List<TransactionEntity> getTransactions(Long accountId) {
        getAccount(accountId);
        return transactionRepository.findByAccountIdOrderByCreatedAtAsc(accountId);
    }

    private AccountEntity getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }


    /**
     * Writes a ledger row to the transaction table. Writes in one db transaction when caller uses @Transactional.
     * @param account
     * @param type
     * @param amount
     * @param balanceAfter
     * @return
     */
    private TransactionEntity writeLedgerRow(
            AccountEntity account, TransactionType type, BigDecimal amount, BigDecimal balanceAfter) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccountId(account.getId());
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        return transactionRepository.save(transaction);
    }
}
