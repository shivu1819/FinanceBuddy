SET @migration_schema = DATABASE();

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @migration_schema
       AND TABLE_NAME = 'budgets'
       AND COLUMN_NAME = 'category_id') = 0,
    'ALTER TABLE budgets ADD COLUMN category_id BIGINT NULL AFTER user_id',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @migration_schema
       AND TABLE_NAME = 'budgets'
       AND INDEX_NAME = 'uk_budgets_user_month') > 0,
    'ALTER TABLE budgets DROP INDEX uk_budgets_user_month',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = @migration_schema
       AND TABLE_NAME = 'budgets'
       AND CONSTRAINT_NAME = 'uk_budgets_user_category_month') = 0,
    'ALTER TABLE budgets ADD CONSTRAINT uk_budgets_user_category_month UNIQUE (user_id, category_id, budget_month)',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = @migration_schema
       AND TABLE_NAME = 'budgets'
       AND CONSTRAINT_NAME = 'fk_budgets_category') = 0,
    'ALTER TABLE budgets ADD CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;
