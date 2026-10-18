-- Transactions: category ownership
ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS fk_transactions_category_legacy;

ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS fk_transactions_category;

ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS fk_transactions_user_category;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_user_category
    FOREIGN KEY (user_id, category_id)
    REFERENCES categories (user_id, id)
    ON DELETE RESTRICT ON UPDATE RESTRICT;

-- Transactions: bank account ownership
ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS fk_transactions_bank_account_legacy;

ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS fk_transactions_bank_account;

ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS fk_transactions_user_bank_account;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_user_bank_account
    FOREIGN KEY (user_id, bank_account_id)
    REFERENCES bank_accounts (user_id, id)
    ON DELETE RESTRICT ON UPDATE RESTRICT;

-- Monthly budgets: category ownership
ALTER TABLE monthly_budgets
    DROP CONSTRAINT IF EXISTS fk_monthly_budgets_category_legacy;

ALTER TABLE monthly_budgets
    DROP CONSTRAINT IF EXISTS fk_monthly_budgets_category;

ALTER TABLE monthly_budgets
    DROP CONSTRAINT IF EXISTS fk_monthly_budgets_user_category;

ALTER TABLE monthly_budgets
    ADD CONSTRAINT fk_monthly_budgets_user_category
    FOREIGN KEY (user_id, category_id)
    REFERENCES categories (user_id, id)
    ON DELETE RESTRICT ON UPDATE RESTRICT;

-- Recurring transactions: category ownership
ALTER TABLE recurring_transactions
    DROP CONSTRAINT IF EXISTS fk_recurring_transactions_category_legacy;

ALTER TABLE recurring_transactions
    DROP CONSTRAINT IF EXISTS fk_recurring_transactions_category;

ALTER TABLE recurring_transactions
    DROP CONSTRAINT IF EXISTS fk_recurring_user_category;

ALTER TABLE recurring_transactions
    ADD CONSTRAINT fk_recurring_user_category
    FOREIGN KEY (user_id, category_id)
    REFERENCES categories (user_id, id)
    ON DELETE RESTRICT ON UPDATE RESTRICT;

-- Recurring transactions: bank account ownership
ALTER TABLE recurring_transactions
    DROP CONSTRAINT IF EXISTS fk_recurring_transactions_bank_account_legacy;

ALTER TABLE recurring_transactions
    DROP CONSTRAINT IF EXISTS fk_recurring_transactions_bank_account;

ALTER TABLE recurring_transactions
    DROP CONSTRAINT IF EXISTS fk_recurring_user_bank_account;

ALTER TABLE recurring_transactions
    ADD CONSTRAINT fk_recurring_user_bank_account
    FOREIGN KEY (user_id, bank_account_id)
    REFERENCES bank_accounts (user_id, id)
    ON DELETE RESTRICT ON UPDATE RESTRICT;
