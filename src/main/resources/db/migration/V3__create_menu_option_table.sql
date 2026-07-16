CREATE TABLE menu_option (
    option_id BIGINT NOT NULL AUTO_INCREMENT,
    menu_id BIGINT NOT NULL,
    size VARCHAR(20) NOT NULL,
    temperature VARCHAR(20) NOT NULL,
    bean_type VARCHAR(100) NOT NULL,
    additional_price BIGINT NOT NULL,
    syrup_available BOOLEAN NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (option_id),
    CONSTRAINT fk_menu_option_menu
        FOREIGN KEY (menu_id) REFERENCES menu (menu_id),
    CONSTRAINT chk_menu_option_size
        CHECK (size IN ('REGULAR', 'LARGE')),
    CONSTRAINT chk_menu_option_temperature
        CHECK (temperature IN ('HOT', 'ICE')),
    CONSTRAINT chk_menu_option_price
        CHECK (additional_price >= 0)
);

CREATE INDEX idx_menu_option_menu
    ON menu_option (menu_id, option_id);
