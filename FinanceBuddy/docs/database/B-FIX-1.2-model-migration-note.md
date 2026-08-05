# B-FIX-1.2 model and data migration note

## Active and legacy models

- `budgets` / `com.financebuddy.backend.budget.Budget` is the active monthly budget model used by the budget and dashboard APIs.
- `goals` / `com.financebuddy.backend.goal.Goal` is the active savings-goal model used by `/api/goals`.
- `monthly_budgets` / `MonthlyBudget` and `savings_goals` / `SavingsGoal` are legacy models. Their entities, repositories, tables, and data are retained and marked deprecated; this milestone does not merge or delete them.
- The active `budgets.budget_month` binary representation is retained for compatibility with existing `YearMonth` data.

## Pre-migration checks

Run these read-only checks before V2 on any populated environment:

```sql
SELECT user_id,
       LOWER(TRIM(name)) AS normalized_name,
       GROUP_CONCAT(id ORDER BY id) AS category_ids,
       COUNT(*) AS duplicate_count
FROM categories
GROUP BY user_id, LOWER(TRIM(name))
HAVING COUNT(*) > 1;

SELECT user_id,
       HEX(budget_month) AS serialized_budget_month,
       GROUP_CONCAT(id ORDER BY id) AS budget_ids,
       COUNT(*) AS duplicate_count
FROM budgets
GROUP BY user_id, budget_month
HAVING COUNT(*) > 1;

SELECT t.id, t.user_id, t.category_id, c.user_id AS category_user_id
FROM transactions t
JOIN categories c ON c.id = t.category_id
WHERE NOT (t.user_id <=> c.user_id);

SELECT t.id, t.user_id, t.bank_account_id, b.user_id AS bank_account_user_id
FROM transactions t
JOIN bank_accounts b ON b.id = t.bank_account_id
WHERE t.bank_account_id IS NOT NULL
  AND t.user_id <> b.user_id;
```

Equivalent ownership checks should be run for `monthly_budgets` and `recurring_transactions` before V3.

## Safe duplicate cleanup

V2 deliberately fails rather than deleting or overwriting duplicates. Back up the database, review category type and meaning, and select the canonical category explicitly before using this template:

```sql
START TRANSACTION;

SET @owner_user_id = NULL;       -- required
SET @canonical_category_id = NULL; -- required
SET @duplicate_category_id = NULL; -- required

SELECT id, user_id, name, type
FROM categories
WHERE user_id = @owner_user_id
  AND id IN (@canonical_category_id, @duplicate_category_id)
FOR UPDATE;

UPDATE transactions
SET category_id = @canonical_category_id
WHERE user_id = @owner_user_id
  AND category_id = @duplicate_category_id;

UPDATE monthly_budgets
SET category_id = @canonical_category_id
WHERE user_id = @owner_user_id
  AND category_id = @duplicate_category_id;

UPDATE recurring_transactions
SET category_id = @canonical_category_id
WHERE user_id = @owner_user_id
  AND category_id = @duplicate_category_id;

DELETE FROM categories
WHERE user_id = @owner_user_id
  AND id = @duplicate_category_id;

COMMIT;
```

For duplicate active budgets, inspect every duplicate row and choose the authoritative limit/status. Archive the other rows outside the production schema before deleting them; the migration does not guess which budget should survive.

If a non-transactional MySQL DDL statement failed partway through V2 or V3, correct the data, run `flyway repair` for that environment, and rerun migration validation before starting the application.
