package com.bantunes82.ledger.dataaccessobject;

import com.bantunes82.ledger.domainobject.OutboxDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxDO, UUID> {
}
