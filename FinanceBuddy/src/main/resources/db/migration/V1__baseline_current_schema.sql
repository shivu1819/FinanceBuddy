-- Baseline for a new FinanceBuddy MySQL schema.
-- Existing non-empty installations are baselined at version 1 and begin with V2.

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_non_expired BIT(1) NOT NULL,
    account_non_locked BIT(1) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    credentials_non_expired BIT(1) NOT NULL,
    email VARCHAR(150) NOT NULL,
    email_verified BIT(1) NOT NULL,
    enabled BIT(1) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'USER') NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    age INT NULL,
    country VARCHAR(100) NULL,
    created_at DATETIME(6) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    date_of_birth DATE NULL,
    full_name VARCHAR(150) NOT NULL,
    monthly_income DECIMAL(15, 2) NULL,
    occupation VARCHAR(100) NULL,
    phone_number VARCHAR(20) NULL,
    updated_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_profiles_user UNIQUE (user_id),
    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    color VARCHAR(20) NULL,
    created_at DATETIME(6) NOT NULL,
    icon VARCHAR(100) NULL,
    name VARCHAR(100) NOT NULL,
    system_default BIT(1) NOT NULL,
    type VARCHAR(20) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    user_id BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_categories_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS bank_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    active BIT(1) NOT NULL,
    bank_name VARCHAR(100) NULL,
    created_at DATETIME(6) NOT NULL,
    current_balance DECIMAL(15, 2) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_bank_accounts_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    amount DECIMAL(15, 2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    description VARCHAR(255) NULL,
    payment_method VARCHAR(50) NOT NULL,
    transaction_date DATE NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    bank_account_id BIGINT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_transactions_category_legacy
        FOREIGN KEY (category_id) REFERENCES categories (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_transactions_bank_account_legacy
        FOREIGN KEY (bank_account_id) REFERENCES bank_accounts (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS recurring_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    active BIT(1) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    description VARCHAR(255) NULL,
    end_date DATE NULL,
    frequency VARCHAR(20) NOT NULL,
    next_run_date DATE NOT NULL,
    start_date DATE NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    bank_account_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_recurring_transactions_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_recurring_transactions_category_legacy
        FOREIGN KEY (category_id) REFERENCES categories (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_recurring_transactions_bank_account_legacy
        FOREIGN KEY (bank_account_id) REFERENCES bank_accounts (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS monthly_budgets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    amount DECIMAL(15, 2) NOT NULL,
    budget_month INT NOT NULL,
    budget_year INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_monthly_budgets_user_category_month_year
        UNIQUE (user_id, category_id, budget_month, budget_year),
    CONSTRAINT fk_monthly_budgets_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_monthly_budgets_category_legacy
        FOREIGN KEY (category_id) REFERENCES categories (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    budget_month VARBINARY(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    monthly_limit DECIMAL(15, 2) NOT NULL,
    status ENUM('EXCEEDED', 'SAFE', 'WARNING') NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_budgets_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS goals (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    description VARCHAR(500) NULL,
    saved_amount DECIMAL(15, 2) NOT NULL,
    status ENUM('COMPLETED', 'IN_PROGRESS') NOT NULL,
    target_amount DECIMAL(15, 2) NOT NULL,
    target_date DATE NULL,
    title VARCHAR(150) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_goals_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS savings_goals (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    current_amount DECIMAL(15, 2) NOT NULL,
    goal_name VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL,
    target_amount DECIMAL(15, 2) NOT NULL,
    target_date DATE NULL,
    updated_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_savings_goals_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS monthly_reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    generated_at DATETIME(6) NOT NULL,
    net_savings DECIMAL(15, 2) NOT NULL,
    report_month INT NOT NULL,
    report_year INT NOT NULL,
    total_expense DECIMAL(15, 2) NOT NULL,
    total_income DECIMAL(15, 2) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_monthly_reports_user_month_year UNIQUE (user_id, report_month, report_year),
    CONSTRAINT fk_monthly_reports_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    read_status BIT(1) NOT NULL,
    title VARCHAR(150) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ai_financial_insights (
    id BIGINT NOT NULL AUTO_INCREMENT,
    generated_at DATETIME(6) NOT NULL,
    insight_type VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(150) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ai_financial_insights_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
