# API Test Samples - JSON Formats

This document contains sample JSON payloads for testing all API endpoints.

## Base URL
```
http://localhost:8080/api
```

## Authentication
All endpoints require Basic Authentication:
- Username: `admin`
- Password: `admin123`

---

## 1. POST /api/database-command

Processes database commands (INSERT, UPDATE, DELETE, SYNC).

### INSERT Command Example
```json
{
  "command_id": "cmd_001_insert_card",
  "command_type": "INSERT",
  "table_name": "access_card",
  "payload": {
    "company_id": 1,
    "provider_id": 1,
    "employee_pk": 101,
    "card_uid": "A1B2C3D4",
    "card_number": "CARD001",
    "issued_at": "2024-01-15T10:00:00",
    "expires_at": "2025-01-15T10:00:00",
    "is_active": 1,
    "created": "2024-01-15T10:00:00",
    "updated": "2024-01-15T10:00:00",
    "created_by": "admin",
    "updated_by": "admin"
  },
  "timestamp": 1705312800000,
  "retry_count": 0
}
```

### UPDATE Command Example
```json
{
  "command_id": "cmd_002_update_employee",
  "command_type": "UPDATE",
  "table_name": "employee",
  "payload": {
    "employee_pk": 101,
    "full_name": "John Doe Updated",
    "email": "john.doe.updated@company.com",
    "is_active": 1,
    "updated": "2024-01-19T12:00:00",
    "updated_by": "admin"
  },
  "timestamp": 1705665600000,
  "retry_count": 0
}
```

### DELETE Command Example
```json
{
  "command_id": "cmd_003_delete_card",
  "command_type": "DELETE",
  "table_name": "access_card",
  "payload": {
    "card_id": 5
  },
  "timestamp": 1705665600000,
  "retry_count": 0
}
```

### SYNC Command Example
```json
{
  "command_id": "cmd_004_sync",
  "command_type": "SYNC",
  "table_name": "access_card",
  "payload": {
    "last_sync_timestamp": 1705665600000
  },
  "timestamp": 1705665600000,
  "retry_count": 0
}
```

### Expected Response (CommandAcknowledgement)
```json
{
  "command_id": "cmd_001_insert_card",
  "status": "applied",
  "reason": "Command executed successfully",
  "timestamp": 1705665600000,
  "affected_rows": 1
}
```

---

## 2. POST /api/command-ack

Receives command acknowledgements from controllers.

### Command Acknowledgement Example
```json
{
  "command_id": "cmd_001_insert_card",
  "status": "applied",
  "reason": "Command executed successfully",
  "timestamp": 1705665600000,
  "affected_rows": 1
}
```

### Expected Response
```json
{
  "status": "received",
  "command_id": "cmd_001_insert_card",
  "timestamp": "1705665600000"
}
```

---

## 3. POST /api/event-log

Processes card scans and system events for real-time access validation.

### Card Scan Event (card_scan)
```json
{
  "event_id": "evt_card_scan_001",
  "event_type": "card_scan",
  "door_id": 1,
  "card_hex": "A1B2C3D4",
  "user_name": "John Doe",
  "details": "Card scanned at main entrance",
  "timestamp": 1705665600000,
  "device_id": "CONTROLLER_001",
  "company_id": 1
}
```

### System Event - Door Opened
```json
{
  "event_id": "evt_door_open_001",
  "event_type": "system_event",
  "door_id": 1,
  "card_hex": "A1B2C3D4",
  "user_name": "John Doe",
  "details": "Door opened",
  "timestamp": 1705665605000,
  "device_id": "CONTROLLER_001",
  "company_id": 1
}
```

### System Event - Door Closed
```json
{
  "event_id": "evt_door_close_001",
  "event_type": "system_event",
  "door_id": 1,
  "card_hex": "A1B2C3D4",
  "user_name": "John Doe",
  "details": "Door closed",
  "timestamp": 1705665610000,
  "device_id": "CONTROLLER_001",
  "company_id": 1
}
```

### Access Granted Response
```json
{
  "event_id": "evt_card_scan_001",
  "event_type": "access_granted",
  "doorType": "MAGNETIC",
  "timestamp": 1705665600000,
  "message": "Access granted"
}
```

### Access Denied Response
```json
{
  "event_id": "evt_card_scan_002",
  "event_type": "access_denied",
  "doorType": null,
  "timestamp": 1705665600000,
  "message": "Card not found or inactive"
}
```

### Complete Flow Example (Sequence)

**Step 1: Card Scan**
```json
POST /api/event-log
{
  "event_id": "evt_001",
  "event_type": "card_scan",
  "door_id": 1,
  "card_hex": "A1B2C3D4",
  "user_name": "John Doe",
  "details": "Card scanned",
  "timestamp": 1705665600000,
  "device_id": "CONTROLLER_001",
  "company_id": 1
}
```

**Step 2: Door Opened (after access granted)**
```json
POST /api/event-log
{
  "event_id": "evt_002",
  "event_type": "system_event",
  "door_id": 1,
  "card_hex": "A1B2C3D4",
  "user_name": "John Doe",
  "details": "Door opened",
  "timestamp": 1705665605000,
  "device_id": "CONTROLLER_001",
  "company_id": 1
}
```

**Step 3: Door Closed**
```json
POST /api/event-log
{
  "event_id": "evt_003",
  "event_type": "system_event",
  "door_id": 1,
  "card_hex": "A1B2C3D4",
  "user_name": "John Doe",
  "details": "Door closed",
  "timestamp": 1705665615000,
  "device_id": "CONTROLLER_001",
  "company_id": 1
}
```

---

## 4. POST /api/server-heartbeat

Receives heartbeat messages from controllers.

### Server Heartbeat Example
```json
{
  "device_id": "CONTROLLER_001",
  "timestamp": 1705665600000,
  "is_online": true,
  "queue_size": 5,
  "db_version_hash": "abc123def456",
  "uptime_seconds": 86400
}
```

### Expected Response (ServerHeartbeatResponse)
```json
{
  "deviceId": "CONTROLLER_001",
  "timestampReceived": 1705665600000,
  "serverStatus": "online",
  "serverTimestamp": 1705665600000
}
```

---

## Testing with cURL

### 1. Database Command (INSERT)
```bash
curl -X POST http://localhost:8080/api/database-command \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "command_id": "cmd_001",
    "command_type": "INSERT",
    "table_name": "access_card",
    "payload": {
      "company_id": 1,
      "provider_id": 1,
      "employee_pk": 101,
      "card_uid": "A1B2C3D4",
      "card_number": "CARD001",
      "issued_at": "2024-01-15T10:00:00",
      "expires_at": "2025-01-15T10:00:00",
      "is_active": 1,
      "created": "2024-01-15T10:00:00",
      "updated": "2024-01-15T10:00:00",
      "created_by": "admin",
      "updated_by": "admin"
    },
    "timestamp": 1705312800000,
    "retry_count": 0
  }'
```

### 2. Event Log (Card Scan)
```bash
curl -X POST http://localhost:8080/api/event-log \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "event_id": "evt_001",
    "event_type": "card_scan",
    "door_id": 1,
    "card_hex": "A1B2C3D4",
    "user_name": "John Doe",
    "details": "Card scanned",
    "timestamp": 1705665600000,
    "device_id": "CONTROLLER_001",
    "company_id": 1
  }'
```

### 3. Server Heartbeat
```bash
curl -X POST http://localhost:8080/api/server-heartbeat \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "CONTROLLER_001",
    "timestamp": 1705665600000,
    "is_online": true,
    "queue_size": 5,
    "db_version_hash": "abc123def456",
    "uptime_seconds": 86400
  }'
```

### 4. Command Acknowledgement
```bash
curl -X POST http://localhost:8080/api/command-ack \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "command_id": "cmd_001",
    "status": "applied",
    "reason": "Command executed successfully",
    "timestamp": 1705665600000,
    "affected_rows": 1
  }'
```

---

## Testing with Postman

1. **Import Collection**: Create a new collection in Postman
2. **Set Authentication**: 
   - Type: Basic Auth
   - Username: `admin`
   - Password: `admin123`
3. **Set Base URL**: `http://localhost:8080/api`
4. **Create Requests**: Use the JSON samples above as request bodies

---

## Testing with Swagger UI

1. Navigate to: `http://localhost:8080/swagger-ui.html`
2. Click "Authorize" button (top right)
3. Enter:
   - Username: `admin`
   - Password: `admin123`
4. Click "Authorize" and "Close"
5. Expand any endpoint and click "Try it out"
6. Paste the JSON samples in the request body
7. Click "Execute"

---

## Sample Data Setup (for testing card validation)

Before testing card scan events, ensure you have test data in the database:

### 1. Insert Company
```json
POST /api/database-command
{
  "command_id": "setup_company",
  "command_type": "INSERT",
  "table_name": "company",
  "payload": {
    "company_name": "Test Company",
    "is_active": 1,
    "created": "2024-01-01T00:00:00",
    "updated": "2024-01-01T00:00:00",
    "created_by": "admin",
    "updated_by": "admin"
  },
  "timestamp": 1704067200000,
  "retry_count": 0
}
```

### 2. Insert Access Group
```json
POST /api/database-command
{
  "command_id": "setup_access_group",
  "command_type": "INSERT",
  "table_name": "access_group",
  "payload": {
    "company_id": 1,
    "group_name": "Employees",
    "description": "Standard employee access",
    "is_active": 1,
    "created": "2024-01-01T00:00:00",
    "updated": "2024-01-01T00:00:00",
    "created_by": "admin",
    "updated_by": "admin"
  },
  "timestamp": 1704067200000,
  "retry_count": 0
}
```

### 3. Insert Employee
```json
POST /api/database-command
{
  "command_id": "setup_employee",
  "command_type": "INSERT",
  "table_name": "employee",
  "payload": {
    "employee_code": "EMP001",
    "company_id": 1,
    "full_name": "John Doe",
    "email": "john.doe@company.com",
    "access_group_id": 1,
    "is_active": 1,
    "created": "2024-01-01T00:00:00",
    "updated": "2024-01-01T00:00:00",
    "created_by": "admin",
    "updated_by": "admin"
  },
  "timestamp": 1704067200000,
  "retry_count": 0
}
```

### 4. Insert Door
```json
POST /api/database-command
{
  "command_id": "setup_door",
  "command_type": "INSERT",
  "table_name": "door",
  "payload": {
    "floor_id": 1,
    "door_code": "MAIN_ENTRANCE",
    "door_number": 1,
    "lock_type": "MAGNETIC",
    "is_active": 1,
    "created": "2024-01-01T00:00:00",
    "updated": "2024-01-01T00:00:00",
    "created_by": "admin",
    "updated_by": "admin"
  },
  "timestamp": 1704067200000,
  "retry_count": 0
}
```

### 5. Insert Access Card
```json
POST /api/database-command
{
  "command_id": "setup_card",
  "command_type": "INSERT",
  "table_name": "access_card",
  "payload": {
    "company_id": 1,
    "provider_id": 1,
    "employee_pk": 1,
    "card_uid": "A1B2C3D4",
    "card_number": "CARD001",
    "issued_at": "2024-01-01T00:00:00",
    "expires_at": "2025-12-31T23:59:59",
    "is_active": 1,
    "created": "2024-01-01T00:00:00",
    "updated": "2024-01-01T00:00:00",
    "created_by": "admin",
    "updated_by": "admin"
  },
  "timestamp": 1704067200000,
  "retry_count": 0
}
```

### 6. Insert Access Group Door (ALLOW access)
```json
POST /api/database-command
{
  "command_id": "setup_access_group_door",
  "command_type": "INSERT",
  "table_name": "access_group_door",
  "payload": {
    "access_group_id": 1,
    "door_id": 1,
    "access_type": "ALLOW",
    "is_active": 1,
    "created": "2024-01-01T00:00:00",
    "updated": "2024-01-01T00:00:00",
    "created_by": "admin",
    "updated_by": "admin"
  },
  "timestamp": 1704067200000,
  "retry_count": 0
}
```

---

## Notes

- All timestamps are in milliseconds (Unix epoch time)
- Date-time strings should be in ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`
- `is_active` fields use `1` for true, `0` for false (TINYINT(1))
- For card validation to work, ensure the card exists and is linked to an employee with proper access group permissions
- The `card_hex` in event-log must match the `card_uid` in the access_card table
