# Huludao Mahjong Agent

Backend-first Huludao mahjong settlement app.

The first milestone is a Spring Boot REST API that calculates and persists settlement results for completed winning hands. The exact Huludao rule details live in `docs/huludao-rules.md` and should be filled with examples before the rule engine grows beyond the current manual-factor calculator.

## Run

```bash
cd backend
mvn test
mvn spring-boot:run
```

API base URL:

```text
http://localhost:8080
```

## Current Scope

- Local profiles
- Settlement calculation API
- Game history persistence
- H2 local development profile
- PostgreSQL production profile
- Pure Java domain calculator

Out of scope for the current milestone:

- Real login
- React UI
- Live game simulation
- Automatic Huludao hand scoring
- Move suggestion

## Profiles

The app defaults to `dev`, which uses an H2 file database at `backend/data/`.
