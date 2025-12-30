package com.bantunes82.ledger.datatransferobject;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionDTO(
        @NotNull(message= "Debit AccountId cannot be null")
        UUID debitAccountId,
        @NotNull(message= "Credit AccountId cannot be null")
        UUID creditAccountId,
        @Positive(message = "Amount must be positive")
        BigDecimal amount) {
}
