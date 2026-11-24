# Support Ticket Management System

## Prerequisites

- JDK 21
- Node.js 20+
- PostgreSQL running locally (dev/demo profile)

## Backend

```bash
cd backend
export DB_URL=jdbc:postgresql://localhost:5432/support_tickets
export DB_USERNAME=support
export DB_PASSWORD='your-local-password'
./gradlew bootRun
```

Create the DB/user once (example):

```sql
CREATE USER support WITH PASSWORD 'your-local-password';
CREATE DATABASE support_tickets OWNER support;
```

If you see `SCRAM-based authentication, but no password was provided`, set `DB_PASSWORD` (or use a gitignored `application-local.yml`). Do not commit passwords.

### Tests (H2 — no Postgres required)

```bash
cd backend
./gradlew test
```

## Frontend

Run in a **separate** terminal from the repo root (not from `backend/`):

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

Open http://localhost:3000 — API defaults to http://localhost:8080.

## Notes

- No authentication in v1.
- Secrets stay in env vars / gitignored local config only.
