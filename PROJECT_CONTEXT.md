# Habitbeat Project Context

## 1. Project Overview

Habitbeat is a virtual body-doubling productivity platform designed to help users maintain focus and accountability through structured collaboration. The core concept revolves around two users forming "Hives" — accountability partnerships where they work together in synchronized focus sessions. The platform emphasizes accountability, scheduling, and consistent session flow to help users build and maintain productive habits through peer support and structured engagement.

## 2. Current Tech Stack

- **Language**: Java 25
- **Framework**: Spring Boot 3.5.7
- **Database**: PostgreSQL 16 (Dockerized)
- **Cache/Session Store**: Redis 7 (Dockerized)
- **Orchestration**: Docker Compose
- **Build Tool**: Maven
- **Version Control**: GitHub
- **API Testing**: Thunder Client
- **Architecture**: Monolithic backend with modular internal structure

## 3. Core Modules

### User & Profile Management
Handles user registration, authentication, profile creation, and user preferences. Manages user settings, timezone configuration, and account lifecycle operations.

### Hive Management
Core system for creating, joining, and managing Hive partnerships. Handles Hive creation, member invitations, role assignments, and Hive-specific configurations and settings.

### Session Management
Orchestrates focus sessions between Hive partners. Manages session scheduling, real-time session tracking, session history, and session-related analytics and reporting.

### Matching System
Facilitates connection between users to form compatible Hives. Implements matching algorithms based on preferences, schedules, goals, and compatibility factors to suggest optimal partnerships.

### Communication & Chat
Provides real-time and asynchronous messaging capabilities within Hives. Enables partners to communicate during and between sessions, share updates, and maintain accountability through direct communication channels.

### Community & Notifications
Manages community features including posts, achievements, and social interactions. Handles notification delivery for events like session reminders, Hive invitations, task completions, and community updates.

### Admin & Analytics
Provides administrative tools and analytics dashboards for monitoring platform health, user engagement metrics, session statistics, and system performance. Supports moderation and platform management functions.

## 4. System Architecture

Habitbeat follows a monolithic Spring Boot backend architecture that interacts with PostgreSQL for persistent data storage and Redis for temporary and runtime data. The system is containerized using Docker Compose for local development and deployment consistency.

**Security Configuration**: Currently disabled in development through `SecurityConfig` to allow open API access during initial development phases.

**API Architecture**: REST-based API design with endpoints organized by functional modules. The backend runs on port 8080 and exposes RESTful endpoints following standard HTTP conventions.

**Code Organization**: Modular package layout under `com.habitbeat.backend.<module>` following clean layered architecture:
- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic and orchestration
- **Repository Layer**: Manages data access and persistence
- **Entity Layer**: Defines domain models and data structures

Each module maintains strict separation of concerns with clear boundaries between layers.

## 5. Current State

- ✅ Backend skeleton initialized and running successfully
- ✅ Docker environment operational (PostgreSQL and Redis containers running)
- ✅ Database connection established and functional
- ✅ Security configured for open development access
- ✅ Repository connected to GitHub (origin: `git@github.com:dparikh07/habitbeat.git`)
- ✅ Complete authentication system implemented with JWT + refresh tokens
- ✅ Database schema designed for multi-auth flows (email/password + OAuth)
- ✅ JWT infrastructure with token rotation and secure cookie handling
- ⏳ Authentication endpoints not yet implemented
- ⏳ No production security implementation

## 6. Authentication System

### Database Schema
Complete authentication schema with UUID primary keys:
- **users**: Core user data with email verification tracking
- **user_profiles**: Extended user information (names, avatar, timezone, etc.)
- **password_credentials**: Hashed passwords with one-to-one user relationship
- **verification_tokens**: Email verification and password reset tokens
- **oauth_states**: OAuth flow state management with PKCE support
- **oauth_identities**: Links users to OAuth providers (Google, etc.)
- **sessions**: Refresh token sessions with rotation and revocation

### JWT Token Model
- **Access JWT**: Short-lived (15 min), signed, sent as `Authorization: Bearer`
- **Refresh Token**: Long-lived (30 days), opaque random bytes, HttpOnly cookie
- **Token Rotation**: Every refresh generates new token, invalidates old one
- **Security**: Refresh tokens stored as hashes, secure cookie attributes

### Authentication Flows Supported
1. **Email/Password**: Traditional signup with email verification
2. **OAuth (Google)**: Social login with account linking
3. **Password Reset**: Secure token-based password recovery
4. **Email Verification**: Required before full account access

### Configuration
Environment variables for JWT and OAuth:
- `JWT_SIGNING_KEY`: HMAC key for JWT signing
- `REFRESH_TOKEN_BYTES`: Random token length (default: 32)
- `COOKIE_DOMAIN`: Domain for refresh token cookies
- `OAUTH_GOOGLE_CLIENT_ID/SECRET`: Google OAuth credentials
- `APP_URL`: Frontend URL for redirects

## 7. Development Conventions

### Primary Keys
- Use **UUIDs** as primary keys across all entities for distributed system compatibility and security benefits.
- Authentication system fully implements UUID-based relationships.

### Layer Separation
- Maintain strict separation between controller, service, and repository layers
- Controllers handle HTTP concerns only
- Services contain all business logic
- Repositories manage data access exclusively

### Data Storage Strategy
- **PostgreSQL**: All persistent data, relationships, and transactional data
- **Redis**: Temporary data, session state, caching, and runtime-only information

### Configuration Management
- Use YAML format (`application.yaml`) for all configuration
- Keep configuration clear, flexible, and environment-agnostic where possible

### Code Quality Standards
- Follow clean code principles: consistent naming conventions, modular design, and self-contained logic
- Use meaningful variable and method names
- Keep methods focused and single-purpose
- Document complex business logic with clear comments
- Maintain consistent code formatting and structure across modules

