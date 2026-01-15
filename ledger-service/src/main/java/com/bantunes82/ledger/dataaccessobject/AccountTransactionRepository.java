package com.bantunes82.ledger.dataaccessobject;

import com.bantunes82.ledger.domainobject.AccountTransactionDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountTransactionRepository extends JpaRepository<AccountTransactionDO, UUID> {
    Optional<AccountTransactionDO> findByIdempotencyKey(String idempotencyKey);
}
