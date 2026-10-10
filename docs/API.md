# API Reference (v1)

All microservice APIs are versioned under `/api/v1`. Through the API gateway (`http://localhost:8080`) every
instance is reached by its service id:

| Service | Service id pattern | Example |
|---|---|---|
| Electricity Provider | `provider-{city}-{a,b,c}` | `/provider-newcastle-a/api/v1/summary` |
| Smart City | `smartcity-{city}` | `/smartcity-durham/api/v1/aggregates` |
| Citizen | `citizen-{city}` | `/citizen-sunderland/api/v1/citizens` |

Cities: `newcastle`, `sunderland`, `durham`, `middlesbrough`, `darlington`. Energy is in kWh; times are ISO 8601
local date-times (`2026-10-10T14:30:00`); months are `yyyy-MM`. Each service also serves Swagger UI at
`/swagger-ui.html` on its own port.

## Error format

Errors use [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807):

```json
{
  "type": "about:blank",
  "title": "Unprocessable Entity",
  "status": 422,
  "detail": "Reading 900 kWh must be more than the previous reading of 4102.511 kWh for NEWCASTLE-A-M001",
  "instance": "/api/v1/readings"
}
```

| Status | When |
|---|---|
| 200 / 201 | Success / created |
| 400 | Missing or malformed fields, bad parameter (for example `months=0`) |
| 404 | Unknown citizen |
| 422 | Business rule broken: reading not higher or not later than the previous one, or manual entry for a smart meter |
| 503 | A provider or the gateway could not be reached |

---

## Electricity Provider

### POST `/api/v1/readings`
Receive a reading in real time. **201 Created**

Request:
```json
{ "citizenId": "NEWCASTLE-A-S012", "meterType": "SMART", "readingKwh": 8421.337, "recordedAt": "2026-10-10T14:30:00" }
```
| Field | Rules |
|---|---|
| `citizenId` | required |
| `meterType` | required, `MANUAL` or `SMART` |
| `readingKwh` | required, 0 or more, and **more than this citizen's previous reading** |
| `recordedAt` | optional (defaults to now), not in the future, **after the previous reading** |

Response:
```json
{ "id": 50231, "citizenId": "NEWCASTLE-A-S012", "meterType": "SMART", "readingKwh": 8421.337,
  "consumptionKwh": 0.012, "recordedAt": "2026-10-10T14:30:00" }
```
Errors: 400, 422.

### GET `/api/v1/readings?citizenId=&limit=50`
Latest readings, newest first; `citizenId` optional, `limit` 1 to 500.

### GET `/api/v1/consumption/monthly?months=12`
Monthly totals for the last N months (1 to 60), oldest first, including the current month. No averages.
```json
[ { "month": "2026-09", "totalKwh": 37210.552, "readingCount": 1520, "citizenCount": 150 },
  { "month": "2026-10", "totalKwh": 9120.004,  "readingCount": 18032, "citizenCount": 150 } ]
```

### GET `/api/v1/summary`
```json
{ "city": "Newcastle", "provider": "A", "readingCount": 25012, "citizenCount": 150,
  "totalKwh": 451230.115, "lastReadingAt": "2026-10-10T14:30:00" }
```

---

## Smart City

### GET `/api/v1/aggregates?months=12`
Stored monthly totals per provider, oldest month first.
```json
[ { "city": "Newcastle", "provider": "A", "month": "2026-09", "totalKwh": 37210.552,
    "readingCount": 1520, "citizenCount": 150, "collectedAt": "2026-10-10T14:30:05" } ]
```
Errors: 400 for months outside 1 to 60.

### GET `/api/v1/summary?months=12`
Totals per provider over the period, with the result of the latest collection.
```json
{ "city": "Newcastle", "totalKwh": 1312004.2,
  "providers": [ { "provider": "A", "totalKwh": 451230.1, "readingCount": 25012,
                   "status": { "provider": "A", "serviceId": "provider-newcastle-a", "reachable": true,
                               "lastAttemptAt": "2026-10-10T14:30:05", "lastSuccessAt": "2026-10-10T14:30:05",
                               "message": null } } ] }
```

### POST `/api/v1/aggregates/collect`
Collect from all providers now. Returns the status of each provider (a provider that is down shows
`"reachable": false` and a message; the others are still collected).

### GET `/api/v1/providers/status`
Status of the most recent collection from each provider.

---

## Citizen

### GET `/api/v1/citizens?provider=a`
Citizens and smart meters, optionally for one provider.
```json
[ { "id": "NEWCASTLE-A-M001", "provider": "A", "meterType": "MANUAL",
    "lastReadingKwh": 12450.221, "lastReadingAt": "2026-10-10T14:00:00" } ]
```
Ids: `{CITY}-{PROVIDER}-M###` for manual citizens (001 to 100), `{CITY}-{PROVIDER}-S###` for smart meters (001 to 050).

### GET `/api/v1/citizens/{id}`
One citizen. Errors: 404.

### POST `/api/v1/citizens/{id}/readings`
Manual entry by a citizen, forwarded to their provider through the gateway straight away. **201 Created**
```json
{ "readingKwh": 12460.5 }
```
Errors: 400 (missing value), 404 (unknown citizen), 422 (not higher than the last reading, or the id is a smart
meter), 503 (provider unreachable).

### GET `/api/v1/simulation/status`
```json
{ "city": "newcastle", "manualCitizens": 300, "smartMeters": 150, "pendingBackfill": 0,
  "ticks": 42, "readingsSent": 14210, "failures": 0, "lastTickAt": "2026-10-10T14:30:00" }
```

---

## Front end (web application, not a JSON API)

| Path | Access | Page |
|---|---|---|
| `/login`, `/logout` | everyone | Sign in / out |
| `/?months=` | signed in | Region overview |
| `/cities/{city}?months=` | signed in | City data table and chart |
| `/providers/{a,b,c}?months=` | signed in | Provider data table across cities |
| `/account/password` | signed in | Change own password |
| `/admin/users` | ADMIN | List, create and delete users |
| `/actuator/health`, `/actuator/info` | health: everyone; info: signed in | Health and version |
