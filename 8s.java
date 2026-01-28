



---

## Security & Encryption

* **Algorithm:** AES-256/CBC/PKCS5Padding
* **Key:** 32-byte AES key (provided by backend)
* **IV:** 16-byte random per request, Base64 encoded, sent in `X-IV` header

**Controller Responsibilities:**

1. Generate random IV per request
2. Encrypt JSON request with AES-256/CBC using key and IV
3. Send to backend with `X-IV` header
4. Decrypt response using same IV
5. Take action based on response (`SUCCESS` → open door, `DENIED` → keep locked)

---

## Endpoints & Payloads

### 1. Validate Access Card (Real-time)

**POST /access/validate**

**Request:**

```json
{
  "controllerMac": "AA:BB:CC:DD:EE:FF",
  "readerUuid": "reader-uuid-123",
  "cardUid": "A1B2C3D4",
  "timestamp": "2026-01-27T15:30:00"
}
```

**Response:**

```json
{
  "result": "SUCCESS",
  "lockType": "MAGNETIC",
  "readerUuid": "reader-uuid-123",
  "reason": null
}
```

### 2. Log Door Event

**POST /access/event**

**Request:**

```json
{
  "controllerMac": "AA:BB:CC:DD:EE:FF",
  "readerUuid": "reader-uuid-123",
  "eventType": "OPEN",
  "timestamp": "2026-01-27T15:30:05"
}
```

**Response:**

```json
{ "status": "OK" }
```

Event Types: `OPEN`, `CLOSE`, `FORCED`

### 3. Database Sync

**POST /controller/db-sync**

**Request:**

```json
{ "controllerMac": "AA:BB:CC:DD:EE:FF" }
```

**Response:**

```json
{
  "readers": [
    { "readerUuid": "reader-uuid-123", "allowedCards": ["A1B2C3D4", "E5F6G7H8"] },
    { "readerUuid": "reader-uuid-456", "allowedCards": ["9Z8Y7X6W"] }
  ]
}
```

### 4. Bulk Event Upload

**POST /controller/bulk-event-logs**

**Request:**

```json
{
  "controllerMac": "AA:BB:CC:DD:EE:FF",
  "events": [
    { "readerUuid": "reader-uuid-123", "cardUid": "A1B2C3D4", "eventType": "OPEN", "eventTime": "2026-01-27T09:00:00" },
    { "readerUuid": "reader-uuid-123", "cardUid": "A1B2C3D4", "eventType": "CLOSE", "eventTime": "2026-01-27T09:00:05" }
  ]
}
```

**Response:**

```json
{ "status": "RECEIVED", "processedCount": 2 }
```

**Note:** Events must be in chronological order.

### 5. Health Check / Ping

**POST /controller/ping**

**Request:**

```json
{ "controllerMac": "AA:BB:CC:DD:EE:FF" }
```

**Response:**

```json
{ "status": "OK", "serverTime": "2026-01-27T15:30:00" }
```

---

**Important Notes:**

* Always encrypt payloads.
* Random IV per request; same IV to decrypt response.
* Offline mode: store events locally and upload via bulk-event-logs.
* Card validation response: `SUCCESS` → open door, `DENIED` → keep locked.
