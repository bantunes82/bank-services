package com.bantunes82.ledger.dataaccessobject;

import com.bantunes82.ledger.domainobject.OutboxEventDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEventDO, UUID> {
}
