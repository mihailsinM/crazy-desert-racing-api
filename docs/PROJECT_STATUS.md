# Project Status

## Project

Crazy Desert Racing Club

## Repository

crazy-desert-racing-api

---

## Current Stage

Backend Foundation

---

## Completed

- Verify license endpoint

Completed:
- Added Spring Security dependency
- Created SecurityConfig
- Configured SecurityFilterChain
- Added in-memory users
- Protected verify-license endpoint
- Protected make-admin endpoint
- Tested ADMIN access (200 OK)
- Tested unauthorized access (403 Forbidden)

- ### Roles
- Role enum
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


1. Spring Security

Next:
- Add password field to User
- Introduce PasswordEncoder
- Create CustomUserDetails
- Create CustomUserDetailsService
- Load users from database
- Replace in-memory authentication
- Prepare JWT authentication

2. JWT Authentication
3. Response DTOs
4. Refactoring
5. Frontend (React + TypeScript)

---

## Learning Goal

Understand every architectural decision and be able to explain the project confidently during technical interviews.