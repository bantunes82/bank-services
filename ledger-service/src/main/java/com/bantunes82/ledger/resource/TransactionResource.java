package com.bantunes82.ledger.resource;

import com.bantunes82.ledger.datatransferobject.TransactionDTO;
import com.bantunes82.ledger.domainobject.AccountTransactionDO;
import com.bantunes82.ledger.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class TransactionResource {

    private final Logger log = LoggerFactory.getLogger(TransactionResource.class);

    private final TransactionService transactionService;

    public TransactionResource(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(value = "/{version}/transactions")
    public ResponseEntity<Void> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        AccountTransactionDO accountTransactionDO = transactionService.transferMoney(
                transactionDTO.debitAccountId(),
                transactionDTO.creditAccountId(),
                transactionDTO.amount(),
                transactionDTO.idempotencyKey(),
                transactionDTO.description());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(accountTransactionDO.getId())
                .toUri();

        log.debug("New transaction created with URI {}", location);
        return ResponseEntity.created(location).build();
    }
}
