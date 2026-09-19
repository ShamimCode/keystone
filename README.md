# Project KEYSTONE — Field Service Management Platform

Built for **Zidio Development** · Client: Meridian Facilities Management
Java Full-Stack Engineering Brief · Submitted by Shamim Mollick ([@ShamimCode](https://github.com/ShamimCode))

---

## Live deployment

| Component                  | URL                                                              |
| -------------------------- | ---------------------------------------------------------------- |
| **Frontend (Vercel)**      | https://keystone-frontend-tawny.vercel.app                       |
| **Backend API (Render)**   | https://keystone-backend-57jc.onrender.com                       |
| **Swagger / OpenAPI docs** | https://keystone-backend-57jc.onrender.com/swagger-ui/index.html |
| **Repository**             | https://github.com/ShamimCode/keystone                           |

> Note: the backend is hosted on Render's free tier, which spins down after periods of inactivity. The first request after idle time may take 30–60 seconds to respond while the service wakes up.

## Seed logins

All seed users share the password **`Password123!`**

| Role       | Email                         |
| ---------- | ----------------------------- |
| Dispatcher | `dispatcher@keystone.example` |
| Technician | `technician@keystone.example` |
| Manager    | `manager@keystone.example`    |
| Customer   | `customer@keystone.example`   |

---

## Overview

KEYSTONE is the field-service platform Meridian Facilities Management uses to run its maintenance operation — from the moment a customer reports a problem to the moment the job is closed and billed. It replaces spreadsheet-and-phone-call coordination with one governed system: dispatchers raise and assign work orders, technicians update them from the field, managers track SLAs and dashboards, and customers can log requests and follow their status.

The platform serves four roles, each seeing only what their role needs:

- **Dispatcher** — creates customers, sites, and work orders; assigns jobs to technicians
- **Technician** — sees only their assigned jobs; starts, holds, and completes work; logs parts and time
- **Manager** — everything a dispatcher can do, plus closing jobs and full visibility
- **Customer** — raises requests for their own sites and tracks only their own work orders

## Stack

| Layer             | Technology                                     |
| ----------------- | ---------------------------------------------- |
| Language          | Java 21                                        |
| Backend framework | Spring Boot (Web, Validation)                  |
| Security          | Spring Security + JWT (stateless, HS256)       |
| Persistence       | Spring Data JPA / Hibernate                    |
| Database          | PostgreSQL                                     |
| Migrations        | Flyway                                         |
| Frontend          | React + TypeScript (Vite)                      |
| Styling           | Tailwind CSS                                   |
| API docs          | springdoc-openapi (Swagger UI)                 |
| Deployment        | Render (backend + Postgres), Vercel (frontend) |

## Architecture summary

The backend is a layered Spring Boot service: thin `@RestController`s handle HTTP concerns only, `@Service` classes hold all business rules and transaction boundaries (including the work-order state machine and role-based authorization logic), Spring Data JPA repositories handle persistence, and PostgreSQL's schema is owned entirely by versioned Flyway migrations — Hibernate is configured with `ddl-auto=validate` and never generates schema itself.

Authentication is stateless JWT: a user logs in once via `POST /api/auth/login`, receives a signed token, and presents it as a `Bearer` header on every subsequent request. No server-side session is created or consulted. Every protected endpoint re-checks the caller's role and, where relevant, their relationship to the specific resource (e.g. a technician can only view or act on work orders assigned to them; a customer can only see their own organization's orders) — this is enforced in the service layer, not just hidden in the UI, so it holds even against direct API calls.

The work-order lifecycle (`NEW → ASSIGNED → IN_PROGRESS → (ON_HOLD) → COMPLETED → CLOSED`, with a `CANCELLED` branch) is encoded as data on the `WorkOrderStatus` enum and enforced by `WorkOrderService`: illegal transitions are rejected with `409 Conflict`, and every transition — including the initial creation — writes an append-only `WorkOrderStatusHistory` row recording who changed what, when. Parts usage and time logging run inside database transactions so that a part's stock count and its usage record either both commit or neither does, and stock is validated against available quantity before any write occurs.

The React frontend is a single role-adaptive dashboard: one codebase reads the logged-in user's role from their JWT-backed session and conditionally renders the relevant actions (create/assign for dispatchers and managers, start/hold/complete/log-time for technicians, read-only history for customers) — the UI adapts to the same authorization rules the backend enforces, though the backend remains the actual source of truth.

## Local setup

### Prerequisites

- JDK 21
- Node.js 18+
- Docker (for local PostgreSQL) or a native PostgreSQL install
- Maven (or use the included wrapper)

### 1. Database

```bash
docker compose up -d db
```

This starts PostgreSQL on `localhost:5432` (or `5433` if you've remapped it locally to avoid a port conflict — see `docker-compose.yml`), with database `keystone`, user/password `keystone`/`keystone`.

### 2. Backend

```bash
cd backend
```

Set a real JWT secret (32+ characters) before running:

```bash
export JWT_SECRET="replace-with-a-long-random-string-at-least-32-chars"
```

Then run:

```bash
./mvnw spring-boot:run
```

Flyway runs all migrations automatically on startup, including seed data (customers, sites, users, and inventory parts).

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Health check: `http://localhost:8080/actuator/health`

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

- App: `http://localhost:5173` (proxies `/api` calls to the backend on port 8080)

## Environment variables

| Variable                              | Purpose                                          | Local default                                |
| ------------------------------------- | ------------------------------------------------ | -------------------------------------------- |
| `DB_HOST` / `DB_PORT`                 | PostgreSQL location                              | `localhost` / `5432`                         |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | Database credentials                             | `keystone` / `keystone` / `keystone`         |
| `JWT_SECRET`                          | HMAC signing key for JWTs (32+ chars)            | **must be set explicitly — no safe default** |
| `JWT_EXPIRATION_MS`                   | Token lifetime in milliseconds                   | `86400000` (24 hours)                        |
| `CORS_ALLOWED_ORIGINS`                | Comma-separated allowed origins for the frontend | `http://localhost:5173`                      |

In production (Render), these are set as environment variables on the web service, with `DB_HOST`/`DB_PORT`/etc. pointing at the managed Render PostgreSQL instance and `CORS_ALLOWED_ORIGINS` set to the deployed Vercel frontend URL.

## Migrations and seed data

Schema and seed data live in `backend/src/main/resources/db/migration/`:

- `V1__init_schema.sql` — creates all tables (`customers`, `sites`, `users`, `work_orders`, `work_order_status_history`, `parts`, `part_usages`, `time_logs`) with foreign keys, check constraints, and indexes matching the domain model.
- `V2__seed_data.sql` — seeds one customer (Meridian Facilities Management) with two sites, one user per role, and three inventory parts. Seed user passwords are stored as verified BCrypt hashes of `Password123!`.

Flyway applies these automatically on every backend startup against whatever database it's pointed at — no manual step required.

## Repository structure

```
keystone/
  backend/      Spring Boot service (controllers, services, domain, repositories, security, migrations)
  frontend/     React + TypeScript SPA (Vite)
  docker-compose.yml
  README.md
```

## API documentation

Full OpenAPI documentation is generated automatically via springdoc and browsable at `/swagger-ui/index.html` on either the local or deployed backend, listing every endpoint grouped by resource (auth, customers, sites, work orders, users) with request/response schemas.

## Security notes

- Passwords are stored only as BCrypt hashes.
- JWTs are signed with HS256 and expire after 24 hours; expired or tampered tokens are rejected by the stateless auth filter.
- Every endpoint other than `/api/auth/login`, Swagger routes, and the health check requires a valid Bearer token.
- Role and resource-ownership checks are enforced in the service layer (via `@PreAuthorize` and explicit ownership checks against the authenticated user), not just hidden in the UI — verified directly against the API with tools like Postman throughout development, including confirming that a technician cannot view or act on another technician's work orders, and that illegal work-order status transitions are rejected with `409 Conflict` regardless of which client sends the request.
