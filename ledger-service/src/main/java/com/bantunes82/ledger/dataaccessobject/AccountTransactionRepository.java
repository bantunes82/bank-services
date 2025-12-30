package com.bantunes82.ledger.dataaccessobject;

import com.bantunes82.ledger.domainobject.AccountTransactionDO;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface AccountTransactionRepository extends CrudRepository<AccountTransactionDO, UUID> {
}
