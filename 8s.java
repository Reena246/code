{
  "database_command": {
    "command_id": "cmd_12345",
    "command_type": "INSERT",
    "table_name": "CardDetails",
    "payload": {
      "card_uid": "9D3B9F1A",
      "card_number": "CARD001",
      "company_id": 1,
      "employee_pk": 1,
      "issued_at": "2024-01-15T10:00:00",
      "expires_at": "2025-01-15T10:00:00"
    },
    "timestamp": 1704700000,
    "retry_count": 0
  },
  "command_ack": {
    "command_id": "cmd_12345",
    "status": "applied",
    "reason": "Command processed successfully",
    "timestamp": 1704700010,
    "affected_rows": 1
  },
  "event_log": {
    "event_id": "evt_12345",
    "event_type": "card_scan",
    "door_id": 1,
    "card_hex": "0x9d3b9f1a",
    "user_name": "John Doe",
    "details": "Card scanned at main entrance",
    "timestamp": 1704700000,
    "device_id": "pi_zero_002",
    "company_id": 1
  },
  "server_heartbeat": {
    "device_id": "pi5_local_server",
    "timestamp": 1704700000,
    "is_online": true,
    "queue_size": 0,
    "db_version_hash": "v1.0",
    "uptime_seconds": 3600
  },
  "database_command_update": {
    "command_id": "cmd_67890",
    "command_type": "UPDATE",
    "table_name": "Employee",
    "payload": {
      "employee_pk": 1,
      "access_group_id": 2,
      "full_name": "Jane Smith"
    },
    "timestamp": 1704700100,
    "retry_count": 0
  },
  "database_command_delete": {
    "command_id": "cmd_11111",
    "command_type": "DELETE",
    "table_name": "AccessCard",
    "payload": {
      "card_id": 5
    },
    "timestamp": 1704700200,
    "retry_count": 0
  },
  "database_command_sync": {
    "command_id": "cmd_22222",
    "command_type": "SYNC",
    "table_name": "Door",
    "payload": {
      "door_id": 1,
      "lock_type": "MAGNETIC"
    },
    "timestamp": 1704700300,
    "retry_count": 0
  },
  "event_log_denied": {
    "event_id": "evt_99999",
    "event_type": "card_scan",
    "door_id": 2,
    "card_hex": "0xINVALID",
    "user_name": "Unknown User",
    "details": "Invalid card scan attempt",
    "timestamp": 1704700400,
    "device_id": "pi_zero_001",
    "company_id": 1
  }
}
