# API Reference

All examples go through the API gateway at `http://localhost:8080`. Every service can also be called directly on
its own port (8081, 8082, 8083), and each has interactive docs at `/swagger-ui.html`.

Money values are in **pounds sterling**, tariffs in **pence**, energy in **kWh**. Dates use ISO 8601
(`2026-10-01T08:00:00`).

## Error format

Every service returns errors as [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807):

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Reading 900 kWh is lower than the previous reading of 3962.51 kWh",
  "instance": "/api/citizens/1/readings"
}
```

| Status | Meaning |
|---|---|
| 400 | Validation failed or a business rule was broken |
| 404 | The provider or citizen does not exist |
| 409 | Duplicate (provider name, citizen email or meter ID) |
| 503 | A downstream service could not be reached |

---

## Electricity Provider service (`/api/providers`)

### List active providers
`GET /api/providers`

```json
[
  { "id": 4, "name": "Coastal Wind Supply", "unitRatePence": 25.40, "standingChargePence": 51.00, "renewablePercent": 85, "active": true },
  { "id": 2, "name": "GreenGrid Energy", "unitRatePence": 26.10, "standingChargePence": 48.20, "renewablePercent": 100, "active": true }
]
```

### Get one provider
`GET /api/providers/{id}` returns a single provider, or 404.

### Cheapest provider
`GET /api/providers/cheapest` returns the active provider with the lowest unit rate.

### Create a provider
`POST /api/providers` returns **201 Created**

```json
{ "name": "Metro Solar", "unitRatePence": 23.80, "standingChargePence": 55.00, "renewablePercent": 70 }
```

| Field | Rule |
|---|---|
| `name` | required, unique |
| `unitRatePence` | required, at least 0.01 |
| `standingChargePence` | required, 0 or more |
| `renewablePercent` | 0 to 100 |

### Update a provider
`PUT /api/providers/{id}` with the same body as create.

### Deactivate a provider
`DELETE /api/providers/{id}` returns **204 No Content**. The provider is hidden from lists but kept for history.

### Bill quote
`GET /api/providers/{id}/quote?kwh=250&days=30` (`days` defaults to 30)

```json
{
  "providerId": 1,
  "providerName": "Northern Power Co",
  "kwh": 250,
  "days": 30,
  "energyCost": 61.25,
  "standingCharge": 16.01,
  "total": 77.26,
  "renewableKwh": 87.50
}
```

---

## Citizen service (`/api/citizens`)

### List citizens
`GET /api/citizens`

```json
[
  { "id": 1, "fullName": "Amelia Clarke", "email": "amelia.clarke@example.com", "postcode": "NE4 7PL", "meterId": "SM-1001", "providerId": 1 }
]
```

### Get one citizen
`GET /api/citizens/{id}`

### Register a citizen
`POST /api/citizens` returns **201 Created**

```json
{ "fullName": "Liam Walker", "email": "liam.walker@example.com", "postcode": "ne1 8st", "meterId": "SM-3001", "providerId": 2 }
```

| Field | Rule |
|---|---|
| `fullName` | required |
| `email` | required, valid email, unique |
| `postcode` | required, valid UK postcode; stored upper case with a space (`NE1 8ST`) |
| `meterId` | required, unique |
| `providerId` | required; checked against the provider service (400 if it does not exist) |

### Update / delete a citizen
`PUT /api/citizens/{id}` (same body as register) and `DELETE /api/citizens/{id}` (204, also deletes readings).

### Switch provider
`PUT /api/citizens/{id}/provider/{providerId}` returns the updated citizen.

### Submit a meter reading
`POST /api/citizens/{id}/readings` returns **201 Created**

```json
{ "readingKwh": 4100.25, "readingAt": "2026-10-10T09:00:00" }
```

`readingAt` is optional (defaults to now) and cannot be in the future. The reading must be **at least** the previous
reading and **later** than it.

### List readings
`GET /api/citizens/{id}/readings` returns readings oldest first.

### Consumption
`GET /api/citizens/{id}/consumption`

```json
{
  "citizenId": 1,
  "postcode": "NE4 7PL",
  "providerId": 1,
  "readingCount": 4,
  "from": "2026-09-01T08:00:00",
  "to": "2026-10-01T08:00:00",
  "days": 30,
  "kwh": 296.31
}
```

### Bill
`GET /api/citizens/{id}/bill` combines the consumption with a quote from the citizen's provider.

```json
{
  "citizenId": 1,
  "fullName": "Amelia Clarke",
  "consumption": { "...": "as above" },
  "quote": { "providerName": "Northern Power Co", "energyCost": 72.60, "standingCharge": 16.01, "total": 88.61, "...": "" }
}
```

### Consumption for every citizen
`GET /api/citizens/consumption` returns a list of consumption summaries (used by the smart city service).

---

## Smart City service (`/api/city`)

### City summary
`GET /api/city/summary`

```json
{
  "generatedAt": "2026-10-10T15:00:00",
  "citizenCount": 6,
  "totalKwh": 1789.40,
  "averageKwhPerCitizen": 298.23,
  "renewableKwh": 812.10,
  "renewableSharePercent": 45.4,
  "co2AvoidedKg": 168.10,
  "byProvider": [
    { "providerId": 2, "providerName": "GreenGrid Energy", "renewablePercent": 100, "citizens": 2, "kwh": 610.20, "renewableKwh": 610.20 }
  ],
  "byDistrict": [
    { "district": "NE1", "citizens": 1, "kwh": 301.10, "averageKwh": 301.10 }
  ]
}
```

### Usage by provider / by district
`GET /api/city/providers` and `GET /api/city/districts` return just the `byProvider` or `byDistrict` list.

### Snapshots
- `POST /api/city/snapshots` saves the current headline figures (**201 Created**)
- `GET /api/city/snapshots` lists saved snapshots, newest first

```json
[
  { "id": 1, "takenAt": "2026-10-10T15:01:12", "citizenCount": 6, "totalKwh": 1789.40, "renewableKwh": 812.10, "co2AvoidedKg": 168.10 }
]
```

The numbers in these examples are illustrative; real values depend on the seed data and any readings you add.
