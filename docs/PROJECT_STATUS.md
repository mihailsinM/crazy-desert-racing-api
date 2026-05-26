# Project Status

## Project

Crazy Desert Racing Club

## Repository

crazy-desert-racing-api

---

## Current Stage

Security and Authentication

---

## Completed

### Security

- Added Spring Security dependency
- Created SecurityConfig
- Configured SecurityFilterChain
- Added Basic Authentication
- Protected verify-license endpoint
- Protected make-admin endpoint
- Tested ADMIN access (200 OK)
- Tested USER access (403 Forbidden)

### Authentication

- Added password field to User
- Added BCrypt password hashing
- Added PasswordEncoder bean
- Added UserRepository.findByEmail()
- Added CustomUserDetailsService
- Replaced InMemoryUserDetailsManager with PostgreSQL authentication
- Tested database authentication

### DTO

- UserCreateRequest
- UserResponse
- Hidden password from API responses

### Roles

- Role enum
- USER role
- ADMIN role
- Default USER role for new users
- Temporary make-admin endpoint

### User

- User entity
- User CRUD
- License category
- License verification reset on category change

### RaceCar

- RaceCar entity
- RaceCar CRUD
- Owner assignment

### Race

- Race entity
- Race CRUD
- Participant limit validation

### RaceRegistration

- Registration entity
- Registration endpoint

### Business Rules

- Race car ownership validation
- License verification validation
- Duplicate registration prevention
- Race capacity validation

### Validation

- Bean Validation
- Request validation

### Exception Handling

- GlobalExceptionHandler
- Custom exceptions

---

## In Progress

- Nothing currently in progress.

---

## Planned Next Steps

1. JWT Authentication
    - AuthenticationRequest DTO
    - AuthenticationResponse DTO
    - JwtService
    - JwtAuthenticationFilter
    - Login endpoint
    - Bearer Token authentication

2. DTO Expansion
    - RaceResponse
    - RaceCarResponse
    - RegistrationResponse

3. Refactoring
    - Encapsulation (private fields)
    - Getters and setters
    - Lombok introduction

4. Frontend (React + TypeScript)

---

## Learning Goal

Understand every architectural decision and be able to explain the project confidently during technical interviews.