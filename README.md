# Artisan Handcraft Marketplace — Backend

Spring Boot 3 (Java 21) REST API with JWT authentication. The React frontend lives in
`../artisanhcmarketplace-frontend-main` and talks to this API at `http://localhost:8080`.

## Prerequisites

| Tool | Install (Windows, PowerShell) |
|------|-------------------------------|
| Java 21 | `winget install EclipseAdoptium.Temurin.21.JDK` |
| Node.js 20+ | `winget install OpenJS.NodeJS.LTS` |
| MySQL 8 *(optional)* | Only needed for the default profile — use the `local` profile to skip it |

Open a **new** terminal after installing so `java` and `npm` are on your PATH.

## Run locally

**1. Backend** (terminal 1, in this folder):

```powershell
# Easiest: no MySQL needed, and finds Java even if JAVA_HOME isn't set in this terminal
.\run-local.cmd

# Same thing by hand (embedded H2 database stored in ./data)
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=local"

# Or, with MySQL running (user root / password root, database created automatically)
.\mvnw spring-boot:run
```

Wait for `Started ArtisanMarketplaceApplication`. Check it at http://localhost:8080/api/products.

**2. Frontend** (terminal 2):

```powershell
cd ..\artisanhcmarketplace-frontend-main
npm install
npm start
```

The app opens at http://localhost:3000.

## Accounts

On first start the database is seeded with categories, sample products and an admin account:

- **Admin:** `admin@artisan.com` / `admin123` (override with `ADMIN_EMAIL` / `ADMIN_PASSWORD`)
- Customers and artisans sign up from the Register page.

## How frontend and backend are linked

- The frontend reads the API address from `REACT_APP_API_URL` in its `.env` (default `http://localhost:8080`).
- The backend allows browser requests from the origins in `CORS_ALLOWED_ORIGINS`
  (default `http://localhost:3000,http://127.0.0.1:3000`).
- After login the frontend stores the JWT and sends it as `Authorization: Bearer <token>`.
- Uploaded product images are saved to `./uploads` and served at `/uploads/<file>`.

## Deploying

- **Backend (Render):** `render.yaml` + `Dockerfile` are ready. Set `JDBC_DATABASE_URL`,
  `JDBC_DATABASE_USERNAME`, `JDBC_DATABASE_PASSWORD`, `JWT_SECRET` (32+ chars),
  `ADMIN_PASSWORD`, and `CORS_ALLOWED_ORIGINS=https://your-frontend-url`.
- **Frontend (Vercel/Netlify):** set `REACT_APP_API_URL=https://your-backend.onrender.com`, then build.

## Environment variables

| Variable | Default | Purpose |
|----------|---------|---------|
| `PORT` | `8080` | HTTP port |
| `JDBC_DATABASE_URL` / `_USERNAME` / `_PASSWORD` | local MySQL, root/root | Database |
| `JWT_SECRET` | dev value | Token signing key |
| `CORS_ALLOWED_ORIGINS` | localhost:3000 | Frontend origins |
| `SEED_DATA` | `true` | Seed sample data on empty DB |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | admin@artisan.com / admin123 | Seeded admin |
| `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` | placeholders | Payments |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | placeholders | Gmail SMTP |
