# Sporty API

A scalable REST API built with Java 21 + Spring Boot 3, featuring JWT authentication, role-based access control, and a React frontend.

---

## Tech Stack

**Backend:** Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA  
**Database:** PostgreSQL (tables auto-created on startup via Hibernate)  
**Auth:** JWT (jjwt 0.12.3), BCrypt password hashing  
**Docs:** Swagger UI via SpringDoc OpenAPI  
**Frontend:** React 18, Vite, Axios, React Router v6

---

## Setup

### Prerequisites
- Java 21
- Maven 3.8+
- PostgreSQL 14+
- Node.js 18+

### Database

```sql
CREATE DATABASE taskflow_db;
```

Tables (`users`, `tasks`) are auto-created by Hibernate when the app starts.

### Backend

```bash
cd backend

# Update DB credentials in src/main/resources/application.properties
# spring.datasource.username=your_user
# spring.datasource.password=your_password

mvn spring-boot:run
```

API runs at: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at: `http://localhost:3000`

---

## API Reference

### Auth — `/api/v1/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/register` | ❌ | Register new user |
| POST | `/login` | ❌ | Login, returns JWT |
| GET | `/me` | ✅ | Get current user profile |

### Tasks — `/api/v1/tasks`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | ✅ USER | Get my tasks (paginated) |
| GET | `/{id}` | ✅ USER | Get task by ID |
| POST | `/` | ✅ USER | Create task |
| PUT | `/{id}` | ✅ USER | Update task |
| DELETE | `/{id}` | ✅ USER | Delete task |

### Admin — `/api/v1/admin`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/tasks` | ✅ ADMIN | All tasks across all users |
| GET | `/users` | ✅ ADMIN | All registered users |

### Sample Request — Register

```json
POST /api/v1/auth/register
{
  "name": "Mark Henry",
  "email": "mark@example.com",
  "password": "henry123"
}
```

### Sample Request — Create Task

```json
POST /api/v1/tasks
Authorization: Bearer <token>

{
  "title": "Build backend API",
  "description": "Spring Boot with JWT auth",
  "status": "IN_PROGRESS",
  "priority": "HIGH"
}
```

---

## Database Schema

```
users
  id          BIGSERIAL PRIMARY KEY
  email       VARCHAR(100) UNIQUE NOT NULL
  name        VARCHAR(60) NOT NULL
  password    VARCHAR(255) NOT NULL  -- bcrypt hash
  role        VARCHAR(20) DEFAULT 'USER'
  created_at  TIMESTAMP NOT NULL

tasks
  id          BIGSERIAL PRIMARY KEY
  title       VARCHAR(200) NOT NULL
  description TEXT
  status      VARCHAR(20) DEFAULT 'PENDING'
  priority    VARCHAR(10) DEFAULT 'MEDIUM'
  user_id     BIGINT REFERENCES users(id)
  created_at  TIMESTAMP NOT NULL
  updated_at  TIMESTAMP
```

---

## Security Practices

- Passwords hashed with BCrypt (strength 12)
- JWT signed with HMAC-SHA256; expires in 24h
- Stateless sessions — no server-side session storage
- Role checked at both route level (`SecurityConfig`) and method level (`@PreAuthorize`)
- Input validated with Jakarta Bean Validation before hitting service layer
- CORS configured to allow only known frontend origins
- Sensitive error messages don't leak internals

---

## Project Structure

```
backend/
├── config/         # Security, Swagger, CORS config
├── controller/     # REST controllers (thin layer)
├── dto/            # Request/response records
├── entity/         # JPA entities (User, Task)
├── exception/      # Custom exceptions + global handler
├── repository/     # Spring Data JPA interfaces
├── security/       # JWT filter
├── service/        # Business logic
└── util/           # JwtUtil

frontend/
├── context/        # Auth context (global state)
├── pages/          # Login, Register, Dashboard
├── components/     # TaskModal
└── services/       # Axios API wrapper
```

---

## Scalability Note

See [SCALABILITY.md](./Scalability.md)
