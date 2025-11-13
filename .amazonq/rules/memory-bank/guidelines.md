# Habitbeat Development Guidelines

## Code Quality Standards

### Formatting and Structure
- **Tab Indentation**: Use tabs for indentation consistently across all Java files
- **Line Length**: Keep lines reasonable length, break long method chains and parameter lists
- **Brace Style**: Opening braces on same line, closing braces on new line
- **Import Organization**: Group imports logically, avoid wildcard imports
- **Method Spacing**: Single blank line between methods for readability

### Naming Conventions
- **Classes**: PascalCase (e.g., `AuthService`, `GlobalExceptionHandler`)
- **Methods**: camelCase with descriptive verbs (e.g., `generateVerificationToken`, `buildBody`)
- **Variables**: camelCase with meaningful names (e.g., `refreshToken`, `verificationToken`)
- **Constants**: UPPER_SNAKE_CASE for static final fields
- **Database Columns**: snake_case (e.g., `email_verified_at`, `profile_setup_done`)

### Documentation Standards
- **Class-level Comments**: Minimal, let code be self-documenting
- **Method Comments**: Only for complex business logic or non-obvious behavior
- **Inline Comments**: Sparingly used, focus on why rather than what
- **Variable Names**: Should be descriptive enough to eliminate need for comments

## Architectural Patterns

### Layered Architecture Implementation
- **Controller Layer**: Handle HTTP concerns only, delegate to services
- **Service Layer**: Contain all business logic, orchestrate operations
- **Repository Layer**: Data access abstraction, extend Spring Data interfaces
- **Model Layer**: Domain entities with JPA annotations

### Dependency Injection Patterns
- **Constructor Injection**: Use `@RequiredArgsConstructor` with Lombok for all dependencies
- **Field Injection**: Avoid `@Autowired` on fields
- **Service Dependencies**: Inject repositories and other services via constructor
- **Configuration Properties**: Inject configuration classes for external settings

### Transaction Management
- **Service-Level Transactions**: Use `@Transactional` on service methods
- **Read-Only Operations**: Mark query operations as `@Transactional(readOnly = true)`
- **Transaction Boundaries**: Keep transactions at appropriate scope, avoid long-running transactions

## Entity and Database Patterns

### Entity Design Standards
- **Primary Keys**: Use UUID with `@GeneratedValue(generator = "UUID")` pattern
- **Timestamps**: Use `@CreationTimestamp` and `@UpdateTimestamp` for audit fields
- **Lombok Integration**: Use `@Data` for getters/setters, `@EqualsAndHashCode(of = "id")` for entities
- **Column Naming**: Use `@Column(name = "snake_case")` for database column mapping
- **Indexes**: Define indexes using `@Index` annotation on `@Table`

### JPA Lifecycle Hooks
- **Data Normalization**: Use `@PrePersist` and `@PreUpdate` for data cleanup (e.g., email lowercasing)
- **Validation**: Implement entity-level validation in lifecycle methods
- **Audit Fields**: Leverage Hibernate annotations for automatic timestamp management

### Repository Patterns
- **Custom Queries**: Use method naming conventions or `@Query` annotations
- **Bulk Operations**: Implement custom methods for batch operations (e.g., `invalidateUserTokens`)
- **Optional Returns**: Use `Optional<T>` for single entity lookups that may not exist

## Security Implementation Patterns

### Authentication Flow Patterns
- **Token Generation**: Use `SecureRandom` for cryptographically secure token generation
- **Password Handling**: Always use `PasswordEncoder` for hashing, never store plain text
- **Token Validation**: Hash tokens for comparison, use time-based expiration
- **Session Management**: Implement token rotation for refresh tokens

### Security Best Practices
- **Email Normalization**: Always lowercase emails before storage and comparison
- **Timing Attack Prevention**: Use consistent response times regardless of user existence
- **Token Expiration**: Implement proper token lifecycle with expiration and consumption tracking
- **Cookie Security**: Use HttpOnly, Secure, and appropriate domain settings

## Error Handling Patterns

### Exception Handling Structure
- **Global Handler**: Use `@ControllerAdvice` for centralized exception handling
- **Consistent Response Format**: Standardize error response structure with timestamp, status, error, message, path
- **Exception Mapping**: Map specific exceptions to appropriate HTTP status codes
- **Error Messages**: Provide clear, user-friendly error messages

### Exception Handling Best Practices
- **Validation Errors**: Extract first field error for user-friendly messages
- **Runtime Exceptions**: Handle gracefully with appropriate HTTP status codes
- **Request Parsing**: Handle malformed JSON and type mismatches explicitly

## Service Layer Patterns

### Business Logic Organization
- **Single Responsibility**: Each service handles one domain area (e.g., AuthService for authentication)
- **Method Granularity**: Break complex operations into smaller, focused methods
- **Error Handling**: Let exceptions bubble up to global handler, don't catch and re-throw
- **State Management**: Use transactional boundaries to ensure data consistency

### Common Service Patterns
- **Conditional Processing**: Use early returns for validation and existence checks
- **Resource Creation**: Follow create-save-notify pattern for entity creation
- **Token Management**: Implement secure token generation and validation patterns
- **Email Integration**: Separate email sending logic into dedicated service

## Utility and Configuration Patterns

### Utility Class Design
- **Component Annotation**: Use `@Component` for Spring-managed utility classes
- **Constructor Injection**: Inject configuration dependencies via constructor
- **Method Naming**: Use descriptive method names that indicate purpose and return type
- **Reusability**: Design utilities for reuse across multiple services

### Configuration Management
- **Properties Classes**: Use dedicated configuration classes with `@ConfigurationProperties`
- **Environment Variables**: Support environment-based configuration override
- **Default Values**: Provide sensible defaults for optional configuration
- **Type Safety**: Use appropriate Java types for configuration values

## Testing and Validation Patterns

### Input Validation
- **Bean Validation**: Use Jakarta validation annotations on DTOs
- **Custom Validation**: Implement custom validators for business rules
- **Request Validation**: Validate at controller layer, handle in global exception handler
- **Data Integrity**: Use database constraints as final validation layer

### Code Organization
- **Package Structure**: Organize by feature/domain rather than technical layer
- **Class Naming**: Use descriptive names that indicate purpose and responsibility
- **Method Organization**: Group related methods together, order by importance/usage
- **Import Management**: Keep imports clean and organized, avoid unused imports