# Crazy Desert Racing Club - Roadmap

## Vision

Crazy Desert Racing Club is a full-stack platform for desert racing enthusiasts.

The project combines:

- racing events
- race car management
- participant management
- community features
- memberships
- festival activities
- media content
- administration tools

The goal is to build a modern platform that can serve as both a portfolio project and a potential startup concept.

---

# Phase 1 - Backend Foundation

Status: Completed

## User Management

- User CRUD
- Driver license category
- License verification

## Race Cars

- RaceCar CRUD
- Car ownership

## Races

- Race CRUD
- Participant limits

## Registrations

- Register a user and a race car for a race
- Ownership validation
- License validation
- Duplicate registration prevention
- Capacity validation

---

# Phase 2 - Security

Status: In Progress

Completed:

- USER role
- ADMIN role
- Endpoint authorization
- Database authentication
- Password hashing with BCrypt
- Login endpoint
- JWT generation
- JWT Bearer Token authentication
- JWT filter
- Protected ADMIN endpoints with JWT

Next:

- Disable Basic Auth
- Make security stateless
- Improve JWT validation
- Handle expired and invalid tokens

---

# Phase 3 - API Improvement

Status: Started

Completed:

- UserCreateRequest
- UserResponse for user creation
- Hidden password from user creation response

Next:

- Hide password from all user responses
- RaceResponse
- RaceCarResponse
- RegistrationResponse
- API cleanup
- Validation improvements

---

# Phase 4 - Frontend

- React
- TypeScript
- Responsive design
- Modern UI
- Login page
- JWT authentication on frontend
- Protected routes

---

# Phase 5 - Membership System

- Bronze membership
- Silver membership
- Gold membership
- VIP membership

---

# Phase 6 - Community

- User profiles
- Teams
- Friends system
- Social features

---

# Phase 7 - Chats

- Global chat
- Event chat
- VIP chat
- Future AI assistant integration

---

# Phase 8 - Festival Features

- Event schedule
- Music festival area
- Tent camp information
- Food zones

---

# Phase 9 - Admin Panel

- User management
- Race management
- Registration management
- Membership management

---

# Phase 10 - Production

- Docker
- CI/CD
- Monitoring
- Deployment

---

# Long-Term Goal

Create a complete full-stack platform that demonstrates:

- backend development
- frontend development
- architecture design
- business logic implementation
- security implementation
- JWT authentication
- product thinking

and can be confidently presented during technical interviews and included in a professional portfolio.