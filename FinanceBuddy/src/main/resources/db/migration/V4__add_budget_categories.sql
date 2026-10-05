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


-- Create the new unique constraint BEFORE removing the old index.
-- The new index starts with user_id, so it can continue supporting
-- the existing foreign key that currently depends on uk_budgets_user_month.
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


-- Now the existing foreign key can use the new index,
-- so the old unique index can be removed safely.
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


-- Add the category foreign key.
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