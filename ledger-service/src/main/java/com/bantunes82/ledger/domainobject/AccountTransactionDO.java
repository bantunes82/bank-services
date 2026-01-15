package com.bantunes82.ledger.domainobject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "account_transaction", indexes = {
        @Index(name = "idx_account_transaction_idempotency", columnList = "idempotencyKey", unique = true),
        @Index(name = "idx_account_transaction_debit_acc", columnList = "debit_account_id"),
        @Index(name = "idx_account_transaction_credit_acc", columnList = "credit_account_id")
})
public class AccountTransactionDO {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(nullable = false, precision = 20, scale = 4)
    private BigDecimal amount;

    @NotNull
    @Column(nullable = false, length = 3)
    private String currency;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debit_account_id", nullable = false)
    private AccountDO debitAccountDO;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_account_id", nullable = false)
    private AccountDO creditAccountDO;

    @Column(length = 1024)
    private String description;

    @NotNull
    @Column(nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onPrePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDebitAccountDO(AccountDO debitAccountDO) {
        this.debitAccountDO = debitAccountDO;
    }

    public void setCreditAccountDO(AccountDO creditAccountDO) {
        this.creditAccountDO = creditAccountDO;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AccountTransactionDO that = (AccountTransactionDO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "AccountTransactionDO{" +
                "id=" + id +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", debitAccountDO=" + debitAccountDO +
                ", creditAccountDO=" + creditAccountDO +
                ", description='" + description + '\'' +
                ", idempotencyKey='" + idempotencyKey + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
