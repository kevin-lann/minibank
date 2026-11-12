package com.minibank.controllers;

import com.minibank.services.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/accounts/{id}/deposits")
    public ResponseEntity<TransactionResponse> deposit(@PathVariable Long id, @Valid @RequestBody DepositRequest req) {
        TransactionResponse transaction = TransactionResponse.from(transactionService.deposit(id, req.amount()));
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/accounts/{id}/withdrawals")
    public ResponseEntity<TransactionResponse> withdraw(@PathVariable Long id, @Valid @RequestBody WithdrawalRequest req) {
        TransactionResponse transaction = TransactionResponse.from(transactionService.withdraw(id, req.amount()));
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transfers")
    public ResponseEntity<List<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest req) {
        List<TransactionResponse> transactions = transactionService
                .transfer(req.fromAccountId(), req.toAccountId(), req.amount())
                .stream()
                .map(TransactionResponse::from)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(transactions);
    }

    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable Long id) {
        List<TransactionResponse> transactions = transactionService.getTransactions(id).stream()
                .map(TransactionResponse::from)
                .toList();
        return ResponseEntity.ok(transactions);
    }
}
