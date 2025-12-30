package com.bantunes82.ledger.service;

import com.bantunes82.ledger.enums.EventType;
import com.bantunes82.ledger.dataaccessobject.AccountRepository;
import com.bantunes82.ledger.dataaccessobject.AccountTransactionRepository;
import com.bantunes82.ledger.dataaccessobject.OutboxRepository;
import com.bantunes82.ledger.domainobject.AccountDO;
import com.bantunes82.ledger.domainobject.AccountTransactionDO;
import com.bantunes82.ledger.domainobject.OutboxDO;
import com.bantunes82.ledger.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import  tools.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.UUID;

import static com.bantunes82.ledger.exception.BusinessException.ErrorCode.ACCOUNT_NOT_FOUND;

@Service
@Validated
@Transactional(propagation = Propagation.SUPPORTS)
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

    @Transactional(propagation = Propagation.REQUIRED)
    public AccountTransactionDO transferMoney(UUID fromAccountUuid, UUID toAccountUuid, BigDecimal amount) {
        AccountDO fromAccountDO = findAccountChecked(fromAccountUuid);
        AccountDO toAccountDO = findAccountChecked(toAccountUuid);

        //TODO: add business validations like sufficient balance, etc.

        AccountTransactionDO accountTransactionDO = new AccountTransactionDO();
        accountTransactionDO.setDescription("Transfer from " + fromAccountDO.getName() + " to " + toAccountDO.getName());
        accountTransactionDO.setAmount(amount);
        accountTransactionDO.setDebitAccount(fromAccountDO);
        accountTransactionDO.setCreditAccount(toAccountDO);
        AccountTransactionDO savedTransaction = accountTransactionRepository.save(accountTransactionDO);

        logger.info("Saved transaction: {}", savedTransaction);

        String payload = jsonMapper.writeValueAsString(savedTransaction);
        OutboxDO outboxDO = new OutboxDO();
        outboxDO.setEventType(EventType.ACCOUNT_TRANSACTION_CREATED);
        outboxDO.setPayload(payload);

        outboxRepository.save(outboxDO);

        logger.info("Saved outbox: {}", outboxDO);

        return savedTransaction;
    }

    private AccountDO findAccountChecked(UUID accountId) {
        return accountRepository.findById(accountId).orElseThrow(() -> new BusinessException("Could not find account with id: " + accountId, ACCOUNT_NOT_FOUND, accountId));
    }
}
