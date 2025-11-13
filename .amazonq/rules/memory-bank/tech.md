# Habitbeat Technology Stack

## Core Technologies

### Backend Framework
- **Spring Boot 3.5.7** - Main application framework
- **Java 21** - Runtime environment and language version
- **Maven** - Build automation and dependency management

### Database & Storage
- **PostgreSQL 16** - Primary database (Dockerized)
- **Redis 7** - Cache and session store (Dockerized)
- **Spring Data JPA** - Database abstraction and ORM
- **Spring Data Redis** - Redis integration

### Security & Authentication
- **Spring Security** - Security framework
- **JWT (JJWT 0.12.3)** - JSON Web Token implementation
  - `jjwt-api` - JWT API
  - `jjwt-impl` - JWT implementation
  - `jjwt-jackson` - Jackson JSON processing
- **OAuth 2.0** - Social authentication support

### Communication & Messaging
- **Spring Boot Mail** - Email service integration
- **Spring Boot Web** - REST API and web layer
- **Spring Boot Validation** - Input validation

### Development Tools
- **Spring Boot DevTools** - Hot reloading and development utilities
- **Lombok** - Code generation and boilerplate reduction
- **Docker Compose** - Local development orchestration

## Build Configuration

### Maven Dependencies
```xml
<!-- Core Spring Boot Starters -->
spring-boot-starter-data-jpa
spring-boot-starter-data-redis
spring-boot-starter-security
spring-boot-starter-validation
spring-boot-starter-web
spring-boot-starter-mail

<!-- Database Drivers -->
postgresql (runtime)

<!-- Security & JWT -->
jjwt-api (0.12.3)
jjwt-impl (0.12.3, runtime)
jjwt-jackson (0.12.3, runtime)

<!-- Development Tools -->
spring-boot-devtools (runtime, optional)
lombok (optional)

<!-- Testing -->
spring-boot-starter-test (test scope)
spring-security-test (test scope)
```

### Build Plugins
- **Maven Compiler Plugin** - Java compilation with Lombok annotation processing
- **Spring Boot Maven Plugin** - Application packaging and execution

## Development Commands

### Local Development
```bash
# Start application with Docker services
docker-compose -f docker/docker-compose.yml up -d
mvn spring-boot:run

# Build application
mvn clean compile

# Run tests
mvn test

# Package application
mvn clean package
```

### Docker Services
```bash
# Start PostgreSQL and Redis
docker-compose -f docker/docker-compose.yml up -d

# Stop services
docker-compose -f docker/docker-compose.yml down

# View service logs
docker-compose -f docker/docker-compose.yml logs
```

## Configuration Management

### Application Configuration (`application.yaml`)
- Database connection settings
- Redis configuration
- JWT token settings
- Email service configuration
- Server port and context settings

### Environment Variables
```bash
# JWT Configuration
JWT_SIGNING_KEY=<hmac-key-for-jwt-signing>
REFRESH_TOKEN_BYTES=32

# Cookie Configuration
COOKIE_DOMAIN=<domain-for-refresh-cookies>

# OAuth Configuration
OAUTH_GOOGLE_CLIENT_ID=<google-oauth-client-id>
OAUTH_GOOGLE_CLIENT_SECRET=<google-oauth-secret>

# Application URLs
APP_URL=<frontend-application-url>
```

## Database Configuration

### PostgreSQL Settings
- **Host**: localhost
- **Port**: 5432
- **Database**: habitbeat
- **Connection Pool**: HikariCP (default)
- **JPA**: Hibernate as ORM provider

### Redis Settings
- **Host**: localhost
- **Port**: 6379
- **Usage**: Session storage, caching, temporary data

## Security Configuration

### JWT Token Configuration
- **Access Token Lifetime**: 15 minutes
- **Refresh Token Lifetime**: 30 days
- **Signing Algorithm**: HMAC-SHA256
- **Token Rotation**: Enabled for refresh tokens

### Cookie Security
- **HttpOnly**: Enabled for refresh tokens
- **Secure**: Environment-dependent
- **SameSite**: Strict policy
- **Domain**: Configurable via environment

## Development Environment

### IDE Support
- **Java 21** compatibility required
- **Lombok plugin** for IDE integration
- **Maven integration** for dependency management
- **Spring Boot support** for application development

### Local Services
- **PostgreSQL 16** running on port 5432
- **Redis 7** running on port 6379
- **Spring Boot application** running on port 8080

### Version Control
- **Git repository**: `git@github.com:dparikh07/habitbeat.git`
- **Branch strategy**: Feature-based development
- **Commit conventions**: Conventional commits recommended