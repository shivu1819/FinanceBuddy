ALTER TABLE bill_splits
    ADD COLUMN description VARCHAR(500) NULL,
    ADD COLUMN bill_date DATE NULL;

CREATE TABLE bill_split_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    bill_split_id BIGINT NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    quantity DECIMAL(15, 3) NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_bill_split_items_split FOREIGN KEY (bill_split_id)
        REFERENCES bill_splits (id) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE bill_split_item_participants (
    item_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    PRIMARY KEY (item_id, participant_id),
    CONSTRAINT fk_bill_split_item_participants_item FOREIGN KEY (item_id)
        REFERENCES bill_split_items (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_bill_split_item_participants_participant FOREIGN KEY (participant_id)
        REFERENCES bill_split_participants (id) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
