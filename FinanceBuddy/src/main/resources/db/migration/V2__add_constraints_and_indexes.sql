-- This migration never removes duplicate business rows automatically.
-- If either unique constraint fails, use the documented cleanup queries,
-- repair the failed Flyway entry, and rerun the migration.

SET @migration_schema = DATABASE();

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @migration_schema
       AND TABLE_NAME = 'categories'
       AND COLUMN_NAME = 'normalized_name') = 0,
    'ALTER TABLE categories ADD COLUMN normalized_name VARCHAR(100) NULL AFTER name',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

UPDATE categories
SET name = TRIM(name),
    normalized_name = LOWER(TRIM(name));

ALTER TABLE categories
    MODIFY COLUMN normalized_name VARCHAR(100) NOT NULL;

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = @migration_schema
       AND TABLE_NAME = 'categories'
       AND CONSTRAINT_NAME = 'uk_categories_user_normalized_name') = 0,
    'ALTER TABLE categories ADD CONSTRAINT uk_categories_user_normalized_name UNIQUE (user_id, normalized_name)',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = @migration_schema
       AND TABLE_NAME = 'budgets'
       AND CONSTRAINT_NAME = 'uk_budgets_user_month') = 0,
    'ALTER TABLE budgets ADD CONSTRAINT uk_budgets_user_month UNIQUE (user_id, budget_month)',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

-- Parent-side keys used by the ownership-aware composite foreign keys in V3.
SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = @migration_schema
       AND TABLE_NAME = 'categories'
       AND CONSTRAINT_NAME = 'uk_categories_user_id') = 0,
    'ALTER TABLE categories ADD CONSTRAINT uk_categories_user_id UNIQUE (user_id, id)',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = @migration_schema
       AND TABLE_NAME = 'bank_accounts'
       AND CONSTRAINT_NAME = 'uk_bank_accounts_user_id') = 0,
    'ALTER TABLE bank_accounts ADD CONSTRAINT uk_bank_accounts_user_id UNIQUE (user_id, id)',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @migration_schema
       AND TABLE_NAME = 'transactions'
       AND INDEX_NAME = 'idx_transactions_user_date') = 0,
    'CREATE INDEX idx_transactions_user_date ON transactions (user_id, transaction_date)',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @migration_schema
       AND TABLE_NAME = 'transactions'
       AND INDEX_NAME = 'idx_transactions_user_type') = 0,
    'CREATE INDEX idx_transactions_user_type ON transactions (user_id, transaction_type)',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @migration_schema
       AND TABLE_NAME = 'transactions'
       AND INDEX_NAME = 'idx_transactions_user_category') = 0,
    'CREATE INDEX idx_transactions_user_category ON transactions (user_id, category_id)',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @migration_schema
       AND TABLE_NAME = 'transactions'
       AND INDEX_NAME = 'idx_transactions_user_bank_account') = 0,
    'CREATE INDEX idx_transactions_user_bank_account ON transactions (user_id, bank_account_id)',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @migration_schema
       AND TABLE_NAME = 'goals'
       AND INDEX_NAME = 'idx_goals_user_status') = 0,
    'CREATE INDEX idx_goals_user_status ON goals (user_id, status)',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @migration_schema
       AND TABLE_NAME = 'goals'
       AND INDEX_NAME = 'idx_goals_user_target_date') = 0,
    'CREATE INDEX idx_goals_user_target_date ON goals (user_id, target_date)',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

-- Supporting child indexes for ownership-aware legacy-model foreign keys.
SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @migration_schema
       AND TABLE_NAME = 'recurring_transactions'
       AND INDEX_NAME = 'idx_recurring_user_category') = 0,
    'CREATE INDEX idx_recurring_user_category ON recurring_transactions (user_id, category_id)',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @migration_schema
       AND TABLE_NAME = 'recurring_transactions'
       AND INDEX_NAME = 'idx_recurring_user_bank_account') = 0,
    'CREATE INDEX idx_recurring_user_bank_account ON recurring_transactions (user_id, bank_account_id)',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;
