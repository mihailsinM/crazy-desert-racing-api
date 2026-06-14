# Project Status

## Project

Crazy Desert Racing Club

## Repository

crazy-desert-racing-api

---

## Current Stage

Frontend MVP Completion and Backend Cleanup
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

### Frontend Integration

- React + TypeScript frontend created
- Login page implemented
- Registration page implemented
- JWT token stored on frontend
- Protected routes added
- Dashboard page implemented
- My Cars page implemented
- Add Car page implemented
- Car Details page implemented
- Edit Car page implemented
- Delete Car functionality implemented
- Races page implemented
- Race Details page implemented
- Register for race from frontend
- Admin race actions added
- VIP Club page implemented
- Desert UI component system created

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

### Race Car Improvements

- Added imageUrl support
- Added imagePosition support
- Supported image positions:
- CENTER
- LEFT
- RIGHT
- TOP
- BOTTOM
- Added GET /race-cars/my endpoint
- Added authenticated car creation flow
- Added car delete flow

### Validation

- Bean Validation
- Request validation

### Exception Handling

- GlobalExceptionHandler
- Custom exceptions

---

## In Progress

- Race participants endpoint
- Marketplace status planning
- Admin Dashboard planning
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

   - ### Entity Encapsulation Refactoring

- Moved entity fields from public to private
- Added getters and setters
- Refactored User entity
- Refactored RaceCar entity
- Refactored Race entity
- Refactored RaceRegistration entity
- Updated service layer to use getters and setters
- Backend starts successfully after refactoring
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