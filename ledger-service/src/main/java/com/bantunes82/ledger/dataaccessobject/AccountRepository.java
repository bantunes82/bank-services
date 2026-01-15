package com.bantunes82.ledger.dataaccessobject;

import com.bantunes82.ledger.domainobject.AccountDO;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountDO, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountDO a WHERE a.id = :id")
    Optional<AccountDO> findByIdForUpdate(@Param("id") UUID id);
}
