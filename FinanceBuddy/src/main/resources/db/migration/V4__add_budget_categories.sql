ALTER TABLE budgets
    ADD COLUMN IF NOT EXISTS category_id BIGINT;

-- Add the new unique constraint before dropping the old one.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_budgets_user_category_month'
    ) THEN
        ALTER TABLE budgets
            ADD CONSTRAINT uk_budgets_user_category_month
            UNIQUE (user_id, category_id, budget_month);
    END IF;
END $$;

-- Drop the old unique constraint if present.
ALTER TABLE budgets
    DROP CONSTRAINT IF EXISTS uk_budgets_user_month;

-- Add category foreign key.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_budgets_category'
    ) THEN
        ALTER TABLE budgets
            ADD CONSTRAINT fk_budgets_category
            FOREIGN KEY (category_id)
            REFERENCES categories (id)
            ON DELETE RESTRICT ON UPDATE RESTRICT;
    END IF;
END $$;
