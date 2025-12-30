package com.bantunes82.ledger.domainobject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "account_transaction")
public class AccountTransactionDO {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(nullable = false, length = 1024)
    private String description;

    @NotNull
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "credit_account_id", nullable = false)
    private AccountDO creditAccountDO;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "debit_account_id", nullable = false)
    private AccountDO debitAccountDO;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onPrePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public AccountDO getCreditAccount() {
        return creditAccountDO;
    }

    public void setCreditAccount(AccountDO creditAccountDO) {
        this.creditAccountDO = creditAccountDO;
    }

    public AccountDO getDebitAccount() {
        return debitAccountDO;
    }

    public void setDebitAccount(AccountDO debitAccountDO) {
        this.debitAccountDO = debitAccountDO;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
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
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", creditAccountDO=" + creditAccountDO +
                ", debitAccountDO=" + debitAccountDO +
                ", createdAt=" + createdAt +
                '}';
    }
}
