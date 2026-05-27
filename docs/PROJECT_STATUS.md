# Project Status

## Project

Crazy Desert Racing Club

## Repository

crazy-desert-racing-api

---

## Current Stage

JWT Authentication

---

## Completed

### Backend Foundation

- User entity
- User CRUD
- RaceCar entity
- RaceCar CRUD
- Race entity
- Race CRUD
- RaceRegistration entity
- Registration endpoint

### Business Rules

- Race car ownership validation
- License verification validation
- Duplicate registration prevention
- Race capacity validation

### Roles

- Role enum
- USER role
- ADMIN role
- Default USER role for new users
- Temporary make-admin endpoint

### Security

- Added Spring Security dependency
- Created SecurityConfig
- Configured SecurityFilterChain
- Added Basic Authentication
- Protected verify-license endpoint
- Protected make-admin endpoint
- Tested USER access (403 Forbidden)
- Tested ADMIN access (200 OK)

### Database Authentication

- Added password field to User
- Added BCrypt password hashing
- Added PasswordEncoder bean
- Added UserRepository.findByEmail()
- Added CustomUserDetailsService
- Replaced in-memory authentication with PostgreSQL authentication
- Tested database authentication

### JWT Authentication

- Added JWT dependencies
- Added AuthenticationRequest DTO
- Added AuthenticationResponse DTO
- Added AuthController
- Added login endpoint
- Added JwtService
- Generated JWT token after successful login
- Added JwtAuthenticationFilter
- Extracted email from Bearer Token
- Loaded user from PostgreSQL using JWT
- Added authentication to SecurityContext
- Tested protected ADMIN endpoint with Bearer Token

### DTO

- UserCreateRequest
- UserResponse
- Hidden password from user creation response

### Validation

- Bean Validation
- Request validation

### Exception Handling

- GlobalExceptionHandler
- Custom exceptions

---

## In Progress

- JWT Security cleanup

---

## Planned Next Steps

1. JWT Security Cleanup
   - Disable Basic Auth
   - Make API stateless
   - Improve JWT validation
   - Handle expired or invalid tokens

2. DTO Expansion
   - Hide password from all user responses
   - Add UserResponse to verify-license
   - Add UserResponse to make-admin
   - Add RaceResponse
   - Add RaceCarResponse
   - Add RegistrationResponse

3. Refactoring
   - Encapsulation
   - Private fields
   - Getters and setters
   - Lombok introduction

4. Frontend
   - React
   - TypeScript
   - Login page
   - JWT storage
   - Protected routes

---

## Learning Goal

Understand every architectural decision and be able to explain the project confidently during technical interviews.