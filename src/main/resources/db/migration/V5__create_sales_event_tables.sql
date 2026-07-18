CREATE TABLE processed_event (
    event_id VARCHAR(36) NOT NULL,
    processed_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (event_id)
);

CREATE TABLE daily_product_sales (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    sales_date DATE NOT NULL,
    quantity INTEGER NOT NULL,
    order_count INTEGER NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_daily_product_sales_product_date UNIQUE (product_id, sales_date),
    CONSTRAINT chk_daily_product_sales_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_daily_product_sales_order_count CHECK (order_count >= 0)
);
