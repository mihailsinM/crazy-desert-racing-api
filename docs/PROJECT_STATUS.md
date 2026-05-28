# Project Status

## Project

Crazy Desert Racing Club

## Repository

crazy-desert-racing-api

---

## Current Stage

JWT Authentication and Frontend Preparation

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
- Disabled Basic Authentication
- Configured stateless session management
- Tested expired JWT behavior
- Tested USER token access
- Tested ADMIN token access

### User Authorization

- Added GET /users/me endpoint
- Current user is resolved from JWT SecurityContext
- USER can access GET /users/me
- USER cannot access GET /users
- USER cannot access GET /users/{id}
- ADMIN can access GET /users
- ADMIN can access GET /users/{id}
- ADMIN can access verify-license
- ADMIN can access make-admin

### DTO

- UserCreateRequest
- UserResponse
- Hidden password from user creation response
- Hidden password from GET /users
- Hidden password from GET /users/{id}
- Hidden password from PUT /users/{id}
- Hidden password from verify-license response
- Hidden password from make-admin response

### Validation

- Bean Validation
- Request validation

### Exception Handling

- GlobalExceptionHandler
- Custom exceptions

---

## In Progress

- Frontend preparation

---

## Upcoming Development Backlog

1. Finish User Security
   - USER can update only own profile
   - USER cannot update other users
   - ADMIN keeps full user management access

2. DTO Expansion
   - RaceResponse
   - RaceCarResponse
   - RegistrationResponse
   - Hide internal entity fields from all API responses

3. JWT Improvements
   - Improve token validation
   - Handle expired tokens cleanly
   - Add refresh token strategy later

4. Frontend Start
   - Create React + TypeScript frontend
   - Login page
   - Store JWT token
   - Call /auth/login
   - Call /users/me
   - Show current user dashboard

5. Refactoring
   - Move entity fields from public to private
   - Add getters and setters
   - Introduce Lombok later

---

## Planned Next Steps

1. Start Frontend
   - Create React + TypeScript app
   - Build login page
   - Connect login page to /auth/login
   - Store JWT token
   - Call /users/me
   - Display current user dashboard

2. Continue Backend Improvements
   - Finish User Security
   - DTO Expansion
   - JWT validation improvements
   - Refactoring

---

## Learning Goal

Understand every architectural decision and be able to explain the project confidently during technical interviews.