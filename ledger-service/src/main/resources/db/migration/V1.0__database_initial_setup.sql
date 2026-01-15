-- Optimized Schema for 1000 TPS Ledger Service

-- 1. Accounts Table with Balance
-- We store balance directly to avoid expensive summation on read
CREATE TABLE account (
                         id UUID PRIMARY KEY,
                         name VARCHAR(256) NOT NULL,
                         balance NUMERIC(20, 4) NOT NULL DEFAULT 0.0000 CHECK (balance >= 0), -- Prevent overdrafts at DB level if desired
                         currency VARCHAR(3) NOT NULL DEFAULT 'USD',
                         version BIGINT NOT NULL DEFAULT 1, -- Optimistic locking support
                         created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                         updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Account Transaction (Immutable Audit Log)
-- Represents the successful movement of funds
CREATE TABLE account_transaction (
                                    id UUID PRIMARY KEY,
                                    amount NUMERIC(20, 4) NOT NULL CHECK (amount > 0),
                                    currency VARCHAR(3) NOT NULL,
                                    debit_account_id UUID NOT NULL REFERENCES account(id),
                                    credit_account_id UUID NOT NULL REFERENCES account(id),
                                    description VARCHAR(1024),
                                    idempotency_key VARCHAR(255) NOT NULL, -- critical for avoiding double-processing
                                    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Unique index for Idempotency
CREATE UNIQUE INDEX idx_account_transaction_idempotency ON account_transaction(idempotency_key);
CREATE INDEX idx_account_transaction_debit_acc ON account_transaction(debit_account_id);
CREATE INDEX idx_account_transaction_credit_acc ON account_transaction(credit_account_id);


-- 3. Outbox Table for Reliable Events
CREATE TABLE outbox_event (
                              id UUID PRIMARY KEY,
                              aggregate_id UUID NOT NULL, -- Refers to account_transaction.id
                              event_type VARCHAR(255) NOT NULL,
                              payload JSONB NOT NULL,
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);


-- Comments regarding 1000 TPS:
-- This schema shifts the "Current Balance" calculation from a Materialized View (expensive refresh)
-- to a Row Update (cheap, strictly serializable per account).
-- To move money:
-- BEGIN;
--   SELECT * FROM account WHERE id IN (debit_id, credit_id) FOR UPDATE; -- Lock rows
--   UPDATE account SET balance = balance - X WHERE id = debit_id;
--   UPDATE account SET balance = balance + X WHERE id = credit_id;
--   INSERT INTO account_transaction ...;
--   INSERT INTO outbox_event ...;
-- COMMIT;
