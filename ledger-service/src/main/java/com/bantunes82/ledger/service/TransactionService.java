package com.bantunes82.ledger.service;

import com.bantunes82.ledger.dataaccessobject.AccountRepository;
import com.bantunes82.ledger.dataaccessobject.AccountTransactionRepository;
import com.bantunes82.ledger.dataaccessobject.OutboxRepository;
import com.bantunes82.ledger.domainobject.AccountDO;
import com.bantunes82.ledger.domainobject.AccountTransactionDO;
import com.bantunes82.ledger.domainobject.OutboxEventDO;
import com.bantunes82.ledger.enums.EventType;
import com.bantunes82.ledger.exception.BusinessException;

import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.bantunes82.ledger.exception.BusinessException.ErrorCode.*;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final OutboxRepository outboxRepository;
    private final JsonMapper jsonMapper;

    private final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(AccountRepository accountRepository,
            AccountTransactionRepository accountTransactionRepository,
            OutboxRepository outboxRepository,
            JsonMapper jsonMapper) {
        this.accountRepository = accountRepository;
        this.accountTransactionRepository = accountTransactionRepository;
        this.outboxRepository = outboxRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public AccountTransactionDO transferMoney(UUID fromAccountUuid, UUID toAccountUuid, BigDecimal amount,
            String idempotencyKey, String description) {
        // 1. Basic Validation
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be positive", INVALID_AMOUNT);
        }
        if (fromAccountUuid.equals(toAccountUuid)) {
            throw new BusinessException("Cannot transfer to the same account", SAME_ACCOUNT_TRANSFER);
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException("Idempotency key is required", IDEMPOTENCY_KEY_REQUIRED);
        }

        // 2. Idempotency Check
        Optional<AccountTransactionDO> existingTx = accountTransactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTx.isPresent()) {
            logger.info("Transaction with idempotency key {} already exists. Returning existing one.", idempotencyKey);
            return existingTx.get();
        }

        // 3. Lock Accounts (Deadlock Prevention: Sort by UUID)
        UUID firstLock = fromAccountUuid.compareTo(toAccountUuid) < 0 ? fromAccountUuid : toAccountUuid;
        UUID secondLock = fromAccountUuid.compareTo(toAccountUuid) < 0 ? toAccountUuid : fromAccountUuid;

        AccountDO fromAccountDO = null;
        AccountDO toAccountDO = null;

        // Lock first
        if (firstLock.equals(fromAccountUuid)) {
            fromAccountDO = accountRepository.findByIdForUpdate(firstLock)
                    .orElseThrow(() -> new BusinessException("Debit account not found", ACCOUNT_NOT_FOUND, firstLock));
            toAccountDO = accountRepository.findByIdForUpdate(secondLock)
                    .orElseThrow(
                            () -> new BusinessException("Credit account not found", ACCOUNT_NOT_FOUND, secondLock));
        } else {
            toAccountDO = accountRepository.findByIdForUpdate(firstLock)
                    .orElseThrow(() -> new BusinessException("Credit account not found", ACCOUNT_NOT_FOUND, firstLock));
            fromAccountDO = accountRepository.findByIdForUpdate(secondLock)
                    .orElseThrow(() -> new BusinessException("Debit account not found", ACCOUNT_NOT_FOUND, secondLock));
        }

        // 4. Business Validation (Sufficient Funds)
        if (fromAccountDO.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient funds", INSUFFICIENT_FUNDS);
        }

        // 5. Execute Transfer
        fromAccountDO.setBalance(fromAccountDO.getBalance().subtract(amount));
        toAccountDO.setBalance(toAccountDO.getBalance().add(amount));

        accountRepository.save(fromAccountDO);
        accountRepository.save(toAccountDO);

        // 6. Create Ledger Transaction
        AccountTransactionDO transactionDO = new AccountTransactionDO();
        transactionDO.setDebitAccountDO(fromAccountDO);
        transactionDO.setCreditAccountDO(toAccountDO);
        transactionDO.setAmount(amount);
        transactionDO.setCurrency(fromAccountDO.getCurrency()); // Assuming same currency
        transactionDO.setIdempotencyKey(idempotencyKey);
        transactionDO.setDescription(description);

        AccountTransactionDO savedTransaction = accountTransactionRepository.save(transactionDO);

        // 7. Create Outbox Event
        OutboxEventDO outboxEventDO = new OutboxEventDO();
        outboxEventDO.setAggregateId(savedTransaction.getId());
        outboxEventDO.setEventType(EventType.ACCOUNT_TRANSACTION_CREATED);
        // Payload
        String payload = jsonMapper.writeValueAsString(savedTransaction.toString());
        outboxEventDO.setPayload(payload);

        outboxRepository.save(outboxEventDO);

        logger.info("Transaction processed successfully: {}", savedTransaction.getId());
        return savedTransaction;
    }
}
