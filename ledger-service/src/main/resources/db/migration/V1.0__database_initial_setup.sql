CREATE TABLE account(
                         id UUID PRIMARY KEY,
                         name VARCHAR(256) NOT NULL,
                         created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                         updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE account_transaction(
                        id UUID PRIMARY KEY,
                        description VARCHAR(1024) NOT NULL,
                        amount NUMERIC(20, 2) NOT NULL CHECK (amount > 0.0),
                        credit_account_id UUID NOT NULL REFERENCES account(id) ON DELETE RESTRICT,
                        debit_account_id UUID NOT NULL REFERENCES account(id) ON DELETE RESTRICT,
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ON account_transaction(credit_account_id);
CREATE INDEX ON account_transaction(debit_account_id);

CREATE VIEW entry(
                            account_id,
                            account_transaction_id,
                            amount,
                            created_at
    ) AS
SELECT
    account_transaction.credit_account_id,
    account_transaction.id,
    account_transaction.amount,
    account_transaction.created_at
FROM
    account_transaction
UNION ALL
SELECT
    account_transaction.debit_account_id,
    account_transaction.id,
    (0.0 - account_transaction.amount),
    account_transaction.created_at
FROM
    account_transaction;


CREATE MATERIALIZED VIEW account_balances(
    account_id,
    balance
    ) AS
SELECT
    account.id,
    COALESCE(sum(entry.amount), 0.0)
FROM
    account
        LEFT OUTER JOIN entry
                        ON account.id = entry.account_id
GROUP BY account.id;

CREATE UNIQUE INDEX ON account_balances(account_id);

CREATE FUNCTION update_balances() RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW account_balances;
    RETURN NULL;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_fix_balance_account_transaction
    AFTER INSERT
        OR UPDATE OF amount, credit_account_id, debit_account_id
        OR DELETE OR TRUNCATE
    ON account_transaction
    FOR EACH STATEMENT
EXECUTE PROCEDURE update_balances();

CREATE TRIGGER trigger_fix_balance_account
    AFTER INSERT
        OR UPDATE OF id
        OR DELETE OR TRUNCATE
    ON account
    FOR EACH STATEMENT
EXECUTE PROCEDURE update_balances();
