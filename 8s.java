-- ===============================================
-- 1️⃣ Controller Table
-- ===============================================
CREATE TABLE IF NOT EXISTS controller (
    controller_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    controller_mac VARCHAR(50) NOT NULL UNIQUE,
    company_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    building_id BIGINT,  -- optional
    status ENUM('REGISTERED','ACTIVE','DISABLED') DEFAULT 'REGISTERED',
    created DATETIME DEFAULT NOW(),
    updated DATETIME DEFAULT NOW(),
    CONSTRAINT fk_controller_company FOREIGN KEY (company_id) REFERENCES company(company_id),
    CONSTRAINT fk_controller_site FOREIGN KEY (site_id) REFERENCES site(site_id),
    CONSTRAINT fk_controller_building FOREIGN KEY (building_id) REFERENCES building(building_id)
);

-- ===============================================
-- 2️⃣ Alter Reader Table
-- ===============================================
ALTER TABLE reader
ADD COLUMN controller_id BIGINT NOT NULL AFTER reader_id,
ADD COLUMN reader_uuid VARCHAR(64) UNIQUE NOT NULL AFTER controller_id,
ADD CONSTRAINT fk_reader_controller FOREIGN KEY (controller_id) REFERENCES controller(controller_id);

-- ===============================================
-- 3️⃣ Alter Audit Table
-- Needed for backend API logging
-- ===============================================
ALTER TABLE audit
ADD COLUMN controller_mac VARCHAR(50) AFTER reader_id,
ADD COLUMN request_received_at DATETIME AFTER event_time,
ADD COLUMN request_processed_at DATETIME AFTER request_received_at,
ADD COLUMN response_sent_at DATETIME AFTER request_processed_at,
ADD COLUMN controller_received_at DATETIME AFTER response_sent_at;

-- Minimal foreign keys to ensure integrity
ALTER TABLE audit
ADD CONSTRAINT fk_audit_reader FOREIGN KEY (reader_id) REFERENCES reader(reader_id),
ADD CONSTRAINT fk_audit_card FOREIGN KEY (card_id) REFERENCES access_card(card_id),
ADD CONSTRAINT fk_audit_door FOREIGN KEY (door_id) REFERENCES door(door_id);

-- ===============================================
-- 4️⃣ Optional DB Sync Table (needed for dbsync API)
-- ===============================================
CREATE TABLE IF NOT EXISTS db_sync_log (
    sync_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    controller_id BIGINT NOT NULL,
    sync_type ENUM('FULL','DELTA') NOT NULL,
    sync_timestamp DATETIME DEFAULT NOW(),
    rows_sent INT,
    CONSTRAINT fk_dbsync_controller FOREIGN KEY (controller_id) REFERENCES controller(controller_id)
);

-- ===============================================
-- 5️⃣ Sample Data for Testing APIs
-- ===============================================

-- Sample Controller
INSERT INTO controller (controller_mac, company_id, site_id, building_id, status, created, updated)
VALUES ('AA:BB:CC:DD:EE:FF', 1, 1, 1, 'REGISTERED', NOW(), NOW());

-- Sample Readers
INSERT INTO reader (door_id, controller_id, reader_uuid, reader_code, is_active, created, updated)
VALUES 
(1, 1, 'UUID-READER-001', 'READER-1', 1, NOW(), NOW()),
(2, 1, 'UUID-READER-002', 'READER-2', 1, NOW(), NOW());

-- Sample Access Card
INSERT INTO access_card (company_id, provider_id, employee_pk, card_uid, card_number, issued_at, expires_at, is_active, created, updated, created_by, updated_by)
VALUES 
(1, 1, 1, 'CARD-HEX-001', '123456', NOW(), DATE_ADD(NOW(), INTERVAL 2 YEAR), 1, NOW(), NOW(), 'admin', 'admin');

-- Sample Access Group
INSERT INTO access_group (company_id, group_name, description, is_active, created, updated, created_by, updated_by)
VALUES (1, 'Employee Access', 'Access to general doors', 1, NOW(), NOW(), 'admin', 'admin');

-- Map Access Group to Door
INSERT INTO access_group_door (access_group_id, door_id, access_type, is_active, created, updated, created_by, updated_by)
VALUES (1, 1, 'ALLOW', 1, NOW(), NOW(), 'admin', 'admin');
