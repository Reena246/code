INSERT INTO reader (
    controller_id,
    door_id,
    reader_code,
    reader_model,
    is_active,
    created,
    updated,
    created_by,
    updated_by,
    reader_uuid
)
VALUES (
    1,
    2,  -- 👈 different door
    'READER-002',
    'HID-RP40',
    1,
    NOW(),
    NOW(),
    'system',
    'system',
    'UUID-READER-002'
);
