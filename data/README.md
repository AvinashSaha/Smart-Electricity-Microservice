# Data folder

Each microservice instance stores its data permanently in its own H2 database file here.
The files are created on the first run and reused on every later run.

| File | Owner | Contents |
|---|---|---|
| `provider-{city}-{a,b,c}.mv.db` | Electricity Provider instance | every meter reading received (citizen id, manual or smart, reading, consumption, time) |
| `smartcity-{city}.mv.db` | Smart City instance | aggregated monthly totals per provider (no averages) |
| `citizen-{city}.mv.db` | Citizen simulation | simulated citizens and smart meters and their last accepted reading |

Cities: `newcastle`, `sunderland`, `durham`, `middlesbrough`, `darlington`.

The committed files contain sample data generated from **one run** of the main program
(12 months of history sent by the simulation, then a few minutes of live readings).

To start again from empty, stop everything and delete the `*.mv.db` files (delete all of them
together, so the citizen and provider databases stay consistent).

To look inside a database while its service is running, open the service's H2 console
(for example http://localhost:8111/h2-console for provider A in Newcastle) with JDBC URL
`jdbc:h2:file:./data/provider-newcastle-a`, user `sa` and an empty password.
