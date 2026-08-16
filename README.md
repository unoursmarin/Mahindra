# Mahindra Geo API

Interactive world map application — explore countries and cities on a 3D globe.

**Stack:** Spring Boot 3.3.4 · Angular 21 · MapLibre GL · PostgreSQL 16

---

## Prerequisites

| Tool | Minimum version |
|------|----------------|
| Docker & Docker Compose | 24+ |
| Java | 17 |
| Maven | 3.9+ (or use the included `mvnw` wrapper) |
| Node.js | 20+ |
| npm | 10+ |

---

## Architecture

```
┌─────────────────────────────────────────────────┐
│  Browser  http://localhost:4200                 │
│  Angular 21 + MapLibre GL (3D globe)            │
└───────────────────┬─────────────────────────────┘
                    │ REST (JSON / GeoJSON)
┌───────────────────▼─────────────────────────────┐
│  Spring Boot API  http://localhost:8080          │
│  Swagger UI       http://localhost:8080/swagger-ui.html │
└───────────────────┬─────────────────────────────┘
                    │ JDBC
┌───────────────────▼─────────────────────────────┐
│  PostgreSQL 16    localhost:5432                 │
│  DB: mahindrageodb   user/pass: mahindra         │
└─────────────────────────────────────────────────┘
```

---

## Quick Start

### 1. Start the database

```bash
docker-compose up -d
```

This starts a PostgreSQL 16 container and exposes port `5432`.
Wait for the health-check to pass (a few seconds):

```bash
docker-compose ps   # STATUS should show "healthy"
```

### 2. Start the backend

```bash
./mvnw spring-boot:run
```

Or with Maven directly:

```bash
mvn spring-boot:run
```

**First launch only:** the backend automatically seeds the database from the bundled
`countries+states+cities.json` file (~195 countries, ~150 000 cities). This takes
roughly 30–60 seconds. You will see progress logs like:

```
Seeded 50/195 countries, 12340 cities so far…
JSON seed complete: 195 countries, 152598 cities loaded in 42000ms
```

Subsequent starts skip the seed (database already populated):

```
Database already populated — skipping JSON seed
```

The API is ready when you see:

```
Started MahindraApplication in X.XXX seconds
```

### 3. Start the frontend

```bash
cd frontend
npm install          # first time only
npm start
```

Open **http://localhost:4200** in your browser.

---

## Resetting the database

If you need a clean re-seed (e.g. after a code change to the seed logic):

```bash
docker-compose down -v   # removes the volume — all data is lost
docker-compose up -d
# then restart the backend
```

---

## API Reference

### Swagger UI

Interactive API explorer available at:
**http://localhost:8080/swagger-ui.html**

### OpenAPI spec

```
GET http://localhost:8080/v3/api-docs
```

### Key endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/countries` | List all countries |
| `GET` | `/api/countries/{id}` | Get country by ID |
| `GET` | `/api/countries/geojson` | Countries as GeoJSON (used by the map) |
| `GET` | `/api/cities` | List cities (supports `?countryId=` filter) |
| `GET` | `/api/cities/geojson?countryId={id}` | Cities as GeoJSON for a country |
| `POST` | `/api/sync` | Trigger manual sync from CountryStateCity API |

---

## Configuration

All configuration lives in `src/main/resources/application.yml`.

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | Backend HTTP port |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/mahindrageodb` | DB connection |
| `countrystatecity.seed-from-json` | `true` | Seed DB from bundled JSON on startup |
| `countrystatecity.sync-on-startup` | `false` | Sync from external CSC API on startup |
| `countrystatecity.api-key` | (bundled key) | Override with `CSC_API_KEY` env var |

To override the API key without editing the file:

```bash
CSC_API_KEY=your_key_here ./mvnw spring-boot:run
```

---

## Running tests

### Backend

```bash
./mvnw test
```

### Frontend

```bash
cd frontend
npm test
```

---

## Project structure

```
Mahindra/
├── docker-compose.yml              # PostgreSQL container
├── pom.xml                         # Maven build (Java 17, Spring Boot 3.3.4)
├── src/
│   ├── main/
│   │   ├── java/com/mahindra/api/
│   │   │   ├── batch/              # JsonSeedRunner — seeds DB from JSON
│   │   │   ├── controller/         # REST controllers
│   │   │   ├── model/              # JPA entities (Country, City)
│   │   │   ├── repository/         # Spring Data repositories
│   │   │   └── service/            # Business logic, GeoJSON building, CSC sync
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── countries+states+cities.json   # bundled seed data (~150k cities)
│   │       └── openapi/api.yaml
│   └── test/
│       └── java/com/mahindra/api/  # Unit tests (Mockito + JUnit 5)
└── frontend/                       # Angular 21 app
    ├── src/app/
    │   ├── components/
    │   │   ├── map/                # MapLibre GL globe + layers
    │   │   ├── country-info/       # Country detail panel
    │   │   └── cities-list/        # City list panel
    │   ├── models/                 # TypeScript interfaces
    │   └── services/               # HTTP services (GeoApiService)
    └── public/assets/              # maplibre worker files
```

---

## Troubleshooting

**Backend fails to start — "Connection refused" to PostgreSQL**
→ Make sure Docker is running and `docker-compose up -d` succeeded. Check with `docker-compose ps`.

**Cities do not appear on the map after zooming in**
→ The boundary GeoJSON (~2 MB) is fetched from GitHub at startup. Wait 2–3 seconds after the map loads before zooming, or check the browser Network tab for the `countries.geojson` request.

**Some countries have no cities**
→ The database was partially seeded before a deduplication fix. Reset the volume (see [Resetting the database](#resetting-the-database)) and restart the backend.

**Port 5432 already in use**
→ Another PostgreSQL instance is running locally. Either stop it or change the host port in `docker-compose.yml`:
```yaml
ports:
  - "5433:5432"   # use 5433 on the host
```
Then update `application.yml` datasource URL accordingly.

**Port 8080 already in use**
→ Change `server.port` in `application.yml`, or pass it at launch:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```
