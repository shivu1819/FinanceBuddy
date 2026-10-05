CREATE TABLE investments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    investment_name VARCHAR(150) NOT NULL,
    investment_type VARCHAR(20) NOT NULL,
    invested_amount DECIMAL(15, 2) NOT NULL,
    current_value DECIMAL(15, 2) NOT NULL,
    investment_date DATE NOT NULL,
    expected_return DECIMAL(5, 2) NOT NULL,
    risk_level VARCHAR(10) NOT NULL,
    notes VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_investments_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_investments_user_date (user_id, investment_date),
    INDEX idx_investments_user_type (user_id, investment_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
