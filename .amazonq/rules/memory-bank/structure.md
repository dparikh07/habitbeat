# Habitbeat Project Structure

## Root Directory Organization
```
habitbeat/
├── backend/           # Spring Boot backend application
├── frontend/          # Frontend application (placeholder)
├── docker/           # Docker orchestration files
├── docs/             # Project documentation
└── .amazonq/         # Amazon Q configuration and rules
```

## Backend Architecture (`backend/`)

### Core Package Structure
```
com.habitbeat.backend/
├── auth/             # Authentication & authorization module
├── config/           # Global configuration classes
├── user/             # User management module (placeholder)
└── BackendApplication.java  # Spring Boot main class
```

### Authentication Module (`auth/`)
Complete authentication system with layered architecture:

```
auth/
├── config/           # Authentication-specific configuration
│   ├── EmailProperties.java     # Email service configuration
│   └── JwtProperties.java       # JWT token configuration
├── controller/       # HTTP request handlers
│   └── AuthController.java      # Authentication endpoints
├── dto/              # Data transfer objects
│   ├── AuthResponse.java        # Authentication response format
│   ├── ResendVerificationRequest.java
│   └── SignupRequest.java       # User registration request
├── model/            # Domain entities
│   ├── OAuthIdentity.java       # OAuth provider linkage
│   ├── OAuthState.java          # OAuth flow state management
│   ├── PasswordCredential.java  # Password storage
│   ├── Session.java             # Refresh token sessions
│   ├── User.java                # Core user entity
│   ├── UserProfile.java         # Extended user information
│   └── VerificationToken.java   # Email verification tokens
├── repository/       # Data access layer
│   ├── PasswordCredentialRepository.java
│   ├── SessionRepository.java
│   ├── UserProfileRepository.java
│   ├── UserRepository.java
│   └── VerificationTokenRepository.java
├── service/          # Business logic layer
│   ├── AuthService.java         # Core authentication logic
│   ├── EmailService.java        # Email delivery service
│   ├── JwtService.java          # JWT token management
│   └── RefreshTokenService.java # Token rotation logic
└── util/             # Utility classes
    └── CookieUtil.java          # HTTP cookie management
```

### Global Configuration (`config/`)
- `GlobalExceptionHandler.java` - Centralized error handling
- `SecurityConfig.java` - Spring Security configuration

### User Module (`user/`)
Placeholder structure for future user management features:
```
user/
├── controller/       # User-related endpoints
├── dto/              # User data transfer objects
├── model/            # User domain entities
├── repository/       # User data access
└── service/          # User business logic
```

## Database Schema Design

### Authentication Tables
- **users**: Core user data with UUID primary keys and email verification tracking
- **user_profiles**: Extended user information (names, avatar, timezone preferences)
- **password_credentials**: Secure password storage with one-to-one user relationship
- **verification_tokens**: Email verification and password reset token management
- **oauth_states**: OAuth flow state with PKCE support for secure social login
- **oauth_identities**: Links users to external OAuth providers (Google, etc.)
- **sessions**: Refresh token session management with rotation and revocation

## Architectural Patterns

### Layered Architecture
- **Controller Layer**: HTTP request/response handling, input validation
- **Service Layer**: Business logic orchestration, transaction management
- **Repository Layer**: Data access abstraction, database operations
- **Model Layer**: Domain entities, data structures

### Security Architecture
- **JWT Access Tokens**: Short-lived (15 min), stateless authentication
- **Refresh Token Rotation**: Long-lived (30 days) tokens with automatic rotation
- **Secure Cookie Handling**: HttpOnly cookies for refresh token storage
- **Multi-Auth Support**: Email/password and OAuth (Google) authentication flows

### Data Storage Strategy
- **PostgreSQL**: Persistent data, relationships, transactional operations
- **Redis**: Temporary data, session state, caching, runtime information
- **Docker Compose**: Local development environment orchestration

## Development Environment

### Build System
- **Maven**: Dependency management and build automation
- **Spring Boot 3.5.7**: Application framework
- **Java 21**: Runtime environment

### Development Tools
- **Docker Compose**: Database and cache orchestration
- **Spring Boot DevTools**: Hot reloading and development utilities
- **Lombok**: Code generation for boilerplate reduction

### Configuration Management
- **YAML Configuration**: `application.yaml` for all application settings
- **Environment Variables**: Sensitive configuration (JWT keys, OAuth credentials)
- **Profile-based Configuration**: Environment-specific settings support