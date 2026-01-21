import uuid
import csv

NUM_ACCOUNTS = 2000  # 2000 accounts
SQL_FILE = "V1.2__add_many_accounts.sql"
CSV_FILE = "accounts.csv"

with open(SQL_FILE, "w") as sql, open(CSV_FILE, "w") as csv_f:
    csv_writer = csv.writer(csv_f)
    csv_writer.writerow(["account_id"]) # Header

    for i in range(NUM_ACCOUNTS):
        acc_id = str(uuid.uuid4())
        name = f"User_{i}"
        # Balance enough for many transfers
        sql.write(f"INSERT INTO account (id, name, balance, currency, version, created_at, updated_at) VALUES ('{acc_id}', '{name}', 1000000.0000, 'USD', 1, NOW(), NOW());\n")
        csv_writer.writerow([acc_id])

print(f"Generated {SQL_FILE} and {CSV_FILE}")
