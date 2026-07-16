INSERT INTO menu_option (
    menu_id,
    size,
    temperature,
    bean_type,
    additional_price,
    syrup_available,
    version,
    created_at,
    updated_at
) VALUES
    (1, 'REGULAR', 'HOT', 'Dark Roast', 0, TRUE, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    (1, 'REGULAR', 'ICE', 'Dark Roast', 0, TRUE, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    (1, 'LARGE', 'HOT', 'Dark Roast', 500, TRUE, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    (1, 'LARGE', 'ICE', 'Dark Roast', 500, TRUE, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    (2, 'REGULAR', 'HOT', 'Medium Roast', 0, TRUE, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    (2, 'REGULAR', 'ICE', 'Medium Roast', 0, TRUE, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    (2, 'LARGE', 'HOT', 'Medium Roast', 500, TRUE, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    (2, 'LARGE', 'ICE', 'Medium Roast', 500, TRUE, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
