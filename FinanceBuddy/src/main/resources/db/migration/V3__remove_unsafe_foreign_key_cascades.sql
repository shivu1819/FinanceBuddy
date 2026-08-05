-- Replace single-column financial-reference foreign keys with ownership-aware
-- composite keys. Every relationship is RESTRICTed so history cannot be
-- deleted through a parent category or bank account.

SET @migration_schema = DATABASE();

SET @foreign_key_name = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE CONSTRAINT_SCHEMA = @migration_schema
      AND TABLE_NAME = 'transactions'
      AND COLUMN_NAME = 'category_id'
      AND REFERENCED_TABLE_NAME = 'categories'
    LIMIT 1
);
SET @migration_sql = IF(
    @foreign_key_name IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE transactions DROP FOREIGN KEY `', REPLACE(@foreign_key_name, '`', '``'), '`')
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;
ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_user_category
        FOREIGN KEY (user_id, category_id) REFERENCES categories (user_id, id)
        ON DELETE RESTRICT ON UPDATE RESTRICT;

SET @foreign_key_name = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE CONSTRAINT_SCHEMA = @migration_schema
      AND TABLE_NAME = 'transactions'
      AND COLUMN_NAME = 'bank_account_id'
      AND REFERENCED_TABLE_NAME = 'bank_accounts'
    LIMIT 1
);
SET @migration_sql = IF(
    @foreign_key_name IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE transactions DROP FOREIGN KEY `', REPLACE(@foreign_key_name, '`', '``'), '`')
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;
ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_user_bank_account
        FOREIGN KEY (user_id, bank_account_id) REFERENCES bank_accounts (user_id, id)
        ON DELETE RESTRICT ON UPDATE RESTRICT;

SET @foreign_key_name = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE CONSTRAINT_SCHEMA = @migration_schema
      AND TABLE_NAME = 'monthly_budgets'
      AND COLUMN_NAME = 'category_id'
      AND REFERENCED_TABLE_NAME = 'categories'
    LIMIT 1
);
SET @migration_sql = IF(
    @foreign_key_name IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE monthly_budgets DROP FOREIGN KEY `', REPLACE(@foreign_key_name, '`', '``'), '`')
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;
ALTER TABLE monthly_budgets
    ADD CONSTRAINT fk_monthly_budgets_user_category
        FOREIGN KEY (user_id, category_id) REFERENCES categories (user_id, id)
        ON DELETE RESTRICT ON UPDATE RESTRICT;

SET @foreign_key_name = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE CONSTRAINT_SCHEMA = @migration_schema
      AND TABLE_NAME = 'recurring_transactions'
      AND COLUMN_NAME = 'category_id'
      AND REFERENCED_TABLE_NAME = 'categories'
    LIMIT 1
);
SET @migration_sql = IF(
    @foreign_key_name IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE recurring_transactions DROP FOREIGN KEY `', REPLACE(@foreign_key_name, '`', '``'), '`')
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;
ALTER TABLE recurring_transactions
    ADD CONSTRAINT fk_recurring_user_category
        FOREIGN KEY (user_id, category_id) REFERENCES categories (user_id, id)
        ON DELETE RESTRICT ON UPDATE RESTRICT;

SET @foreign_key_name = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE CONSTRAINT_SCHEMA = @migration_schema
      AND TABLE_NAME = 'recurring_transactions'
      AND COLUMN_NAME = 'bank_account_id'
      AND REFERENCED_TABLE_NAME = 'bank_accounts'
    LIMIT 1
);
SET @migration_sql = IF(
    @foreign_key_name IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE recurring_transactions DROP FOREIGN KEY `', REPLACE(@foreign_key_name, '`', '``'), '`')
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;
ALTER TABLE recurring_transactions
    ADD CONSTRAINT fk_recurring_user_bank_account
        FOREIGN KEY (user_id, bank_account_id) REFERENCES bank_accounts (user_id, id)
        ON DELETE RESTRICT ON UPDATE RESTRICT;
