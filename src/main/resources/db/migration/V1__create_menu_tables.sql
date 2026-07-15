CREATE TABLE menu_category (
    category_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    display_order INTEGER NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (category_id),
    CONSTRAINT uk_menu_category_name UNIQUE (name)
);

CREATE TABLE menu (
    menu_id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    price BIGINT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (menu_id),
    CONSTRAINT fk_menu_category
        FOREIGN KEY (category_id) REFERENCES menu_category (category_id),
    CONSTRAINT chk_menu_price CHECK (price >= 0)
);

CREATE INDEX idx_menu_category_display
    ON menu_category (display_order, category_id);

CREATE INDEX idx_menu_category_menu
    ON menu (category_id, menu_id);
