# Habitbeat System Specification

## 1) Modules Overview (Conceptual)
- **User & Profile Management**: Accounts, profiles, preferences, identity.
- **Hive Management**: Two-person accountability units (“Hives”), invitations, roles, lifecycle.
- **Session Management**: Scheduling, starting/stopping focus sessions, attendance, metrics.
- **Matching System**: Pairing users into Hives based on preferences, availability, goals.
- **Communication & Chat**: Real-time/asynchronous messaging within a Hive or session.
- **Community & Notifications**: Posts, reactions, achievements; notification delivery.
- **Admin & Analytics**: Operational visibility, moderation, metrics, dashboards.

## 2) Schemas (High-Level Data Model Outlines)
Note: High-level shapes only; not SQL. All primary keys are UUIDs.

- **User**: id, name, email (unique), timezone, createdAt, updatedAt
- **Hive**: id, name/label, status (active/archived), createdAt, updatedAt
- **HiveMember**: id, hiveId, userId, role (owner/member), joinedAt, status
- **HiveRequest**: id, hiveId, requesterId, targetUserId, state (pending/accepted/rejected), createdAt
- **Session**: id, hiveId, startAt, endAt, duration, status, notes
- **SessionParticipant**: id, sessionId, userId, presence (joined/left), contribution metrics
- **Task**: id, userId or hiveId, title, status, dueAt, completedAt, tracking fields
- **CommunityPost**: id, authorUserId, hiveId (optional), content, type, createdAt, reactionsCount
- **Notification**: id, userId, type, payload (context), readAt, createdAt
- **Match**: id, userId, matchedUserId (or hiveId), score, criteria snapshot, createdAt
- **Habit**: id, userId, title, cadence (e.g., daily/weekly), streak, createdAt, updatedAt

## 3) APIs (Representative Design)
Base URL: `/api` (backend runs on 8080). JSON request/response. HTTP status codes aligned with REST.

### 3.1 User APIs (implemented)
- GET `/api/users`
  - Response: 200 OK `[User]`
- GET `/api/users/{id}`
  - Path: `id` (UUID)
  - Response: 200 OK `User` | 404 Not Found
- POST `/api/users`
  - Body: `{ name: string, email?: string, timezone?: string }`
  - Response: 201 Created `User` | 400 Bad Request (email exists)
- PUT `/api/users/{id}`
  - Path: `id` (UUID)
  - Body: Partial update payload (name/email/timezone)
  - Response: 200 OK `User` | 404 Not Found | 400 Bad Request (email exists)
- DELETE `/api/users/{id}`
  - Path: `id` (UUID)
  - Response: 204 No Content | 404 Not Found

### 3.2 Hive APIs (planned)
- POST `/api/hives` → create hive (returns hive)
- POST `/api/hives/{hiveId}/invite` → invite user (creates HiveRequest)
- POST `/api/hives/{hiveId}/accept` → accept invite
- GET `/api/hives/{hiveId}` → get hive details with members
- GET `/api/hives/me` → list hives for current user

### 3.3 Session APIs (planned)
- POST `/api/sessions` → schedule or start a session
- POST `/api/sessions/{id}/start` → transition to active
- POST `/api/sessions/{id}/stop` → finalize, compute duration/metrics
- GET `/api/sessions/{id}` → session detail
- GET `/api/sessions?hiveId=...` → list by hive

### 3.4 Matching APIs (planned)
- POST `/api/match/preview` → submit preferences, get preview matches
- POST `/api/match/confirm` → confirm and propose a hive pairing

### 3.5 Communication & Chat (planned)
- GET `/api/chat/{hiveId}` → fetch messages
- POST `/api/chat/{hiveId}` → send message

### 3.6 Community & Notifications (planned)
- GET `/api/community/feed` → timeline posts
- POST `/api/community/posts` → create post
- GET `/api/notifications` → unread notifications
- POST `/api/notifications/read` → mark as read

### 3.7 Admin & Analytics (planned)
- GET `/api/admin/metrics` → platform-level KPIs
- GET `/api/admin/hives` → hive inventory and statuses

## 4) Module Responsibilities & Business Logic (Context)

### User & Profile Management
- Responsible for identity and preferences. Email must be unique. Timezone used for scheduling, reminders, analytics grouping.
- Business rules: Prevent duplicate emails; allow partial updates; maintain createdAt/updatedAt.

### Hive Management
- Exactly two active members per Hive in standard mode. Invitations managed via HiveRequest; member roles (owner/member).
- Business rules: Enforce 2-member constraint; handle invitation lifecycle; allow archival rather than hard delete.

### Session Management
- Sessions represent structured focus intervals with start/stop and optional scheduling.
- Business rules: A session belongs to a Hive; duration computed on stop; track participation for both members.

### Matching System
- Pairs users based on goals, availability, timezone, and preferences; outputs compatibility score.
- Business rules: Avoid repeat poor matches; allow rematch cooldown; consider historical session reliability.

### Communication & Chat
- In-Hive chat for coordination and accountability; optional session-scoped threads.
- Business rules: Retain reasonable history; moderate harmful content; support basic reactions.

### Community & Notifications
- Community posts for broader engagement; notifications for reminders, invites, session events.
- Business rules: Respect quiet hours; deduplicate notifications; mark read/unread state.

### Admin & Analytics
- Operational dashboards: active sessions, DAU/WAU, retention, match success rate, average session duration.
- Business rules: Admin-only endpoints; protect PII; aggregate for analytics.

## 5) Input/Output Examples (Representative)

### Create User (POST /api/users)
Request:
```
{
  "name": "Alex",
  "email": "alex@example.com",
  "timezone": "America/New_York"
}
```
Response 201:
```
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Alex",
  "email": "alex@example.com",
  "timezone": "America/New_York",
  "createdAt": "2025-11-10T02:35:00",
  "updatedAt": "2025-11-10T02:35:00"
}
```

### Get User (GET /api/users/{id})
Response 200:
```
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Alex",
  "email": "alex@example.com",
  "timezone": "America/New_York",
  "createdAt": "2025-11-10T02:35:00",
  "updatedAt": "2025-11-10T02:35:00"
}
```

## 6) Security & Environment
- Development: Security disabled via `SecurityConfig` (all requests permitted).
- Future: JWT-based auth (register/login), password hashing (BCrypt), role-based endpoints.
- Environments: Local via Docker Compose (PostgreSQL, Redis). Backend on port 8080.

## 7) Review Notes
- Use UUIDs for all primary keys (done for User).
- Add Flyway for versioned schema migrations once models stabilize.
- Implement authentication before launching matching and session flows.
- Keep modules cohesive and expose narrow, purposeful APIs.
