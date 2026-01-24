-- Step 1: Add columns safely (allow NULLs initially)
ALTER TABLE reader
ADD COLUMN controller_id BIGINT NULL AFTER reader_id,
ADD COLUMN reader_uuid VARCHAR(64) UNIQUE;

-- Step 2: Assign controller_id for existing rows
-- Make sure the controller you assign exists in the controller table (example: controller_id = 1)
UPDATE reader SET controller_id = 1 WHERE controller_id IS NULL;

-- Step 3: Generate reader UUIDs for existing rows (replace with proper UUIDs later if needed)
UPDATE reader SET reader_uuid = CONCAT('READER-UUID-', reader_id) WHERE reader_uuid IS NULL;

-- Step 4: Add foreign key constraint
ALTER TABLE reader
ADD CONSTRAINT fk_reader_controller FOREIGN KEY (controller_id) REFERENCES controller(controller_id);

-- Step 5: Make controller_id NOT NULL now that all rows have valid controller_id
ALTER TABLE reader
MODIFY COLUMN controller_id BIGINT NOT NULL;
