# Spec: Huludao Mahjong Settlement App

## Objective

Build a backend-first Huludao mahjong application for local use. The first milestone is a Spring Boot REST API that can calculate and persist a completed game's settlement for a local profile from game configuration, a winning hand, and other non-winner hands.

The scoring engine must stay isolated from REST and persistence so the Huludao-specific rules can be filled in and tested as the rule document matures.

## Tech Stack

- Java 17
- Spring Boot 3.3.x
- Maven
- PostgreSQL
- H2 for local development
- JUnit 5
- React + Vite + TypeScript later, after the backend API stabilizes

## Commands

Run from the repository root unless noted.

```bash
cd backend && mvn test
cd backend && mvn spring-boot:run
cd backend && SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

## Project Structure

```text
backend/                 Spring Boot backend
backend/src/main/java    Application source
backend/src/test/java    Unit tests
docs/                    Product, rule, and architecture notes
```

## Code Style

Keep domain code framework-free where possible. REST DTOs can be Java records; entities stay in persistence packages.

Conventions:

- Domain classes live under `domain` and do not import Spring or JPA.
- Application services orchestrate domain + repositories.
- Controllers only validate HTTP shape and delegate.
- Tests for rules should prefer pure domain unit tests over Spring context tests.
- Local dev uses H2; production uses PostgreSQL.

## Testing Strategy

- Unit tests cover settlement arithmetic and validation.
- Each rule example in `docs/huludao-rules.md` should become a unit test before implementation.
- REST tests should be added once the API shape is stable.
- Database integration tests can be added later if PostgreSQL-specific behavior matters.

## Boundaries

- Always: keep settlement logic independent from Spring/JPA, add examples before implementing rule details, preserve explainable settlement output.
- Ask first: adding real login/auth, changing the API response shape, introducing non-standard libraries, adding migration tooling, changing database technology.
- Never: assume hidden opponent hand information, hard-code unverified Huludao rules, store secrets in the repo.

## Success Criteria

- Backend compiles and unit tests pass.
- Local profiles can be created and listed.
- Settlement calculation returns one delta per player and the deltas sum to zero.
- A calculated settlement can be persisted as game history for a profile.
- Huludao-specific rules have a dedicated spec file and a clear place to be implemented.

## Open Questions

- Exact Huludao scoring table and special cases.
- Tile notation standard for user input.
- Whether future real authentication should use sessions or JWT.
