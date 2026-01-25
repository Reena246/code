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
VALUES
(1, 1, 'ITSR-DOOR-1', 'HID-RP40', 1, NOW(), NOW(), 'system', 'system', UUID()),
(1, 2, 'ITSR-DOOR-2', 'HID-RP40', 1, NOW(), NOW(), 'system', 'system', UUID());

