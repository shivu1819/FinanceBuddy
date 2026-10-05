CREATE TABLE IF NOT EXISTS bill_splits (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    subtotal DECIMAL(15, 2) NOT NULL,
    tax_amount DECIMAL(15, 2) NOT NULL,
    tip_amount DECIMAL(15, 2) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    split_type VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_bill_splits_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_bill_splits_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS bill_split_participants (
    id BIGINT NOT NULL AUTO_INCREMENT,
    bill_split_id BIGINT NOT NULL,
    participant_name VARCHAR(150) NOT NULL,
    share_amount DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_bill_split_participants_split_name
        UNIQUE (bill_split_id, participant_name),
    CONSTRAINT fk_bill_split_participants_split
        FOREIGN KEY (bill_split_id) REFERENCES bill_splits (id)
        ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
