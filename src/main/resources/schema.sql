CREATE TABLE IF NOT EXISTS expenses (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(150)   NOT NULL,
    amount        DECIMAL(12, 2) NOT NULL,
    category      VARCHAR(50)    NOT NULL,
    expense_date  DATE           NOT NULL,
    notes         VARCHAR(500),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    KEY idx_expenses_category (category),
    KEY idx_expenses_date (expense_date)
);
