
. POST /api/event-log
{
  "event_id": "evt_20240111_001",
  "event_type": "access_granted",  // Options: "access_granted", "access_denied", "card_scan", "system_event"
  "door_id": "Main Entrance",
  "card_hex": 2639994650,  // Number, not string
  "user_name": "John Doe",
  "details": "Access granted successfully",
  "timestamp": 1704700000,  // Unix timestamp in seconds
  "device_id": "pi_zero_001"
}
2. POST /api/database-command
{
  "command_id": "cmd_20240111_001",
  "command_type": "INSERT",  // Options: "INSERT", "UPDATE", "DELETE", "SYNC", "SYNC_RESPONSE"
  "table_name": "CardDetails",
  "payload": {
    "company_id": 1,
    "provider_id": 1,
    "employee_pk": 1,
    "card_uid": "9d3b9f1a",
    "card_number": "CARD001",
    "is_active": true
  },
  "timestamp": 1704700000,
  "retry_count": 0  // Optional, default: 0
}

3. POST /api/command-ack

{
  "command_id": "cmd_20240111_001",
  "status": "applied",  // Options: "applied", "failed", "pending"
  "reason": "Command executed successfully",
  "timestamp": 1704700010,
  "affected_rows": "1"  // String, not number
}

. POST /api/server-heartbeat
{
  "device_id": "pi5_local_server",
  "timestamp": 1704700000,
  "is_online": true,  // Boolean
  "queue_size": 42,  // Number
  "db_version_hash": "v1.0",
  "uptime_seconds": 3600  // Number
}
