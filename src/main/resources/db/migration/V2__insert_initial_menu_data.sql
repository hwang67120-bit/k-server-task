INSERT INTO menu_category (
    name,
    display_order,
    created_at,
    updated_at
) VALUES
    ('커피', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('케이크', 2, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('티', 3, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('에이드', 4, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('사이드 스낵', 5, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));

INSERT INTO menu (
    category_id,
    name,
    price,
    version,
    created_at,
    updated_at
) VALUES
    ((SELECT category_id FROM menu_category WHERE name = '커피'), '아메리카노', 4500, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ((SELECT category_id FROM menu_category WHERE name = '커피'), '카페 라떼', 5000, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ((SELECT category_id FROM menu_category WHERE name = '케이크'), '치즈 케이크', 6000, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ((SELECT category_id FROM menu_category WHERE name = '티'), '유자차', 5500, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ((SELECT category_id FROM menu_category WHERE name = '에이드'), '레몬 에이드', 5800, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ((SELECT category_id FROM menu_category WHERE name = '사이드 스낵'), '초콜릿 쿠키', 2500, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
