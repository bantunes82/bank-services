package com.bantunes82.ledger.dataaccessobject;

import com.bantunes82.ledger.domainobject.AccountDO;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface AccountRepository extends CrudRepository<AccountDO, UUID> {
}
