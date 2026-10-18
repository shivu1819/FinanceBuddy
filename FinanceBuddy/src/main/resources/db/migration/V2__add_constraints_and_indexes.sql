-- Normalize category names
ALTER TABLE categories
    ADD COLUMN IF NOT EXISTS normalized_name VARCHAR(100);

UPDATE categories
SET name = TRIM(name),
    normalized_name = LOWER(TRIM(name))
WHERE normalized_name IS NULL;

UPDATE categories
SET name = TRIM(name),
    normalized_name = LOWER(TRIM(name));

ALTER TABLE categories
    ALTER COLUMN normalized_name SET NOT NULL;

-- Unique constraints
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_categories_user_normalized_name'
    ) THEN
        ALTER TABLE categories
            ADD CONSTRAINT uk_categories_user_normalized_name
            UNIQUE (user_id, normalized_name);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_budgets_user_month'
    ) THEN
        ALTER TABLE budgets
            ADD CONSTRAINT uk_budgets_user_month
            UNIQUE (user_id, budget_month);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_categories_user_id'
    ) THEN
        ALTER TABLE categories
            ADD CONSTRAINT uk_categories_user_id UNIQUE (user_id, id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_bank_accounts_user_id'
    ) THEN
        ALTER TABLE bank_accounts
            ADD CONSTRAINT uk_bank_accounts_user_id UNIQUE (user_id, id);
    END IF;
END $$;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_transactions_user_date
    ON transactions (user_id, transaction_date);

CREATE INDEX IF NOT EXISTS idx_transactions_user_type
    ON transactions (user_id, transaction_type);

CREATE INDEX IF NOT EXISTS idx_transactions_user_category
    ON transactions (user_id, category_id);

CREATE INDEX IF NOT EXISTS idx_transactions_user_bank_account
    ON transactions (user_id, bank_account_id);

CREATE INDEX IF NOT EXISTS idx_goals_user_status
    ON goals (user_id, status);

CREATE INDEX IF NOT EXISTS idx_goals_user_target_date
    ON goals (user_id, target_date);

CREATE INDEX IF NOT EXISTS idx_recurring_user_category
    ON recurring_transactions (user_id, category_id);

CREATE INDEX IF NOT EXISTS idx_recurring_user_bank_account
    ON recurring_transactions (user_id, bank_account_id);
