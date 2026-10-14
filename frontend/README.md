# Rawaa frontend

This Angular app is the browser interface for the Spring Boot API in the repository root.

## Run locally

1. Start MySQL and the Spring Boot server on port 8080.
2. In this directory, run `pnpm install` if dependencies are missing.
3. Run `pnpm start` and open `http://127.0.0.1:4200/`.

Angular's development proxy forwards `/api` calls to `http://localhost:8080`, so the frontend uses relative API URLs. The proxy is for local development; a deployment needs equivalent routing or a configured API base URL.

## Where to find each part

| File | Purpose |
| --- | --- |
| `src/main.ts` | Starts Angular and registers routing, HTTP, and the JWT interceptor. |
| `src/app/app.component.ts` | Shared header, login/logout navigation, and page outlet. |
| `src/app/app.routes.ts` | URL routes and the dashboard guard. |
| `src/app/auth.service.ts` | Login, registration, token storage, expiry check, and logout. |
| `src/app/auth.interceptor.ts` | Adds `Authorization: Bearer <token>` to API requests and clears rejected sessions. |
| `src/app/login.component.ts` | Renter/owner registration, sign-in, and readable validation errors. |
| `src/app/dashboard.component.ts` | Renter and owner dashboards for reservations and contracts. |
| `src/app/map.component.ts` | Loads buildings and apartments, filters available listings, displays Leaflet markers, asks for browser location, and sends rental requests. |
| `src/styles.css` | Shared visual styles and responsive layout. |
| `proxy.conf.json` | Routes local `/api` requests to Spring Boot. |

## User flow

1. A visitor can browse available apartments and filter by district or maximum rent.
2. A renter registers or signs in. The backend returns a JWT with the account role and ID.
3. The frontend stores the session in `sessionStorage`, which lasts for the current browser tab. The route guard checks token expiry before opening the dashboard. The interceptor sends the token with API requests. Spring Boot validates authorization; frontend checks alone do not secure API access.
4. The map can request the browser's location for a signed-in account. The browser controls permission and returns an estimated location with an accuracy radius. Rawaa does not save this location in the account.
5. A renter selects an available apartment, chooses a start date and rental length, and sends a request. The API creates a `PENDING` reservation.
6. The owner reviews the request in their dashboard and approves or rejects it. An approved request becomes `APPROVED`, and the owner can create a `PENDING` contract with an optional security deposit.
7. The renter sees the contract terms in their dashboard and can accept or reject them. The existing API permits acceptance on or after the contract start date. Acceptance changes the contract to `ACTIVE` and the apartment to `RENTED`. No payment is collected by this frontend.

Contract email is optional in local development. Set `EMAIL_NOTIFICATIONS_ENABLED=true` only when working SMTP credentials are configured.

## Verify

Run `pnpm run build` in this directory. For an end-to-end check, sign in as a renter, open an available listing, submit a request, and confirm the API returns a pending reservation.

The demo apartment added in the local MySQL database is data, not a tracked source file. A Git push would not copy that apartment to another database.
