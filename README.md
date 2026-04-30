# Team Task Manager

A collaborative task management starter app with JWT authentication, role-based access, project management, task assignment, and a dashboard.

## Tech Stack

- Backend: Java 21, Spring Boot, Spring Security, Spring Data JPA
- Database: H2 in-memory for local development
- Frontend: React + Vite

## Project Structure

```text
backend/   Spring Boot REST API
frontend/  React UI
```

## Step 1: Run Backend

```bash
cd backend
mvn spring-boot:run
```

Backend runs at `http://localhost:8080`.

## Step 2: Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173`.

## Demo Accounts

The backend seeds two users on startup:

| Role | Email | Password |
| --- | --- | --- |
| Admin | admin@teamtask.local | Admin@123 |
| Member | member@teamtask.local | Member@123 |

## API Summary

Auth:

- `POST /api/auth/signup`
- `POST /api/auth/login`

Users:

- `GET /api/users/profile`
- `GET /api/users/team`

Projects:

- `POST /api/projects` Admin only
- `GET /api/projects`
- `GET /api/projects/{id}`
- `DELETE /api/projects/{id}` Admin only

Tasks:

- `POST /api/tasks` Admin only
- `GET /api/tasks`
- `PUT /api/tasks/{id}`
- `DELETE /api/tasks/{id}` Admin only

Dashboard:

- `GET /api/dashboard`

## Step-by-Step Build Plan

1. Backend setup, entities, repositories, and auth.
2. Project and task APIs with role-based access.
3. React pages for login/signup, dashboard, projects, tasks, and profile.
4. Frontend/backend integration.
5. Testing, validation improvements, and deployment.
