UPDATE reader
SET
    reader_code = 'READER-001',
    reader_model = 'HID-RP40',
    updated = NOW(),
    updated_by = 'system'
WHERE door_id = 1;

ALTER TABLE reader DROP INDEX reader_door_id;
