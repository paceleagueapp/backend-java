# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

PaceLeague — a Spring Boot backend for a running-record and ranking service. Users log runs, the server computes pace/calories/score, and members are ranked by season/tier.

## Commands

```bash
./gradlew bootRun          # run locally (Windows: gradlew.bat bootRun)
./gradlew test             # run all tests (JUnit 5)
./gradlew test --tests "com.example.paceleague.SomeTest"   # run a single test class
./gradlew clean build      # full build (used by CI/Docker, run with -x test to skip tests)
```

Local run requires a `local` Spring profile with env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (see `application-local.yml`), plus a running MySQL and Redis (Redis defaults to `127.0.0.1:6379`).

Docker: `docker build -t paceleague .` / `docker run -d -p 8080:8080 --name paceleague paceleague`. Deploy is via GitHub Actions on push to `main`: builds the jar, pushes to AWS ECR, then triggers deployment on EC2 via SSM (`.github/workflows/deploy.yml`, `.github/ssm-commands.json`).

## Architecture

Each domain package under `com.example.paceleague` follows a strict layered structure: `controller → service → repository`, with `entity`/`dto` alongside. Controllers must stay thin (no business logic, no `@Transactional`); services own transactions and business logic; repositories are the only DB access point. See `AGENT.md` for the full rule set this codebase is meant to follow (Java/Gradle only, no Kotlin/Maven, minimal abstraction, no unrequested design patterns or multi-module split).

Domains: `member` (auth), `record` (running logs), `rank` (individual score/tier), `ranking` (leaderboard), `season`, `appversion` (mobile force/soft update check), `common` (cross-cutting config/response/error).

Note the `rank` vs `ranking` split — they are separate packages by design, not duplicates: `rank` answers "what's my score/tier" (`GET /api/rank/me`), `ranking` answers "show me the leaderboard" (`GET /api/ranking/getRanking`).

### Auth flow

JWT-based, stateless (`SecurityConfig`, `sessionCreationPolicy(STATELESS)`).

- Public endpoints: `/api/member/join`, `/login`, `/reissue`, `/logout`, `/api/app/version-check`, Swagger paths. Everything else requires a valid access token.
- `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`, validates the access token, and puts a `JwtAuthenticationFilter.AuthPrincipal(memberSno)` on the `Authentication`. Controllers pull the current user via `((AuthPrincipal) authentication.getPrincipal()).memberSno()` — this pattern is repeated in every authenticated controller, not centralized in a resolver/annotation.
- Access tokens are short-lived JWTs (`JwtTokenProvider`); refresh tokens are opaque random strings stored in Redis with TTL (`RefreshTokenService`, key prefix `refresh:`), not JWTs. `/reissue` rotates the refresh token (old one revoked, new one issued).
- Password hashing via `BCryptPasswordEncoder` (Spring Security is used only for auth mechanics here, not full MVC integration).

### Response/error conventions

- All controller responses wrap in `ResponseApi<T>` (`success`/`code`/`message`/`data`/`timestamp`) via `ResponseApi.success(...)`.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) maps `IllegalArgumentException` → 400 and everything else → 500, both as `ApiError` bodies with `ErrorCode`. Domain/service-layer validation failures should throw `IllegalArgumentException` to get a proper 400 rather than falling through to the generic 500 handler.

### Record calculations

`RecordSummaryCalculator` (in `record.service`) is the pace/calorie math: distance in meters → km, pace = duration / distance rounded HALF_UP, calories = weight(kg) × distance(km) (a rough linear approximation, not a real MET-based formula). `RankTierPolicy` (in `rank.policy`) derives a `RankTier` from a raw score by finding the highest tier whose `minScore` the score clears.

Known gap: `RecordController.getMonthAll` hardcodes `weightKg = 70` instead of reading the member's actual weight (marked with a TODO in the code).

## Testing

Only `PaceleagueApplicationTests` (context-load smoke test) currently exists — `AGENT.md` calls for JUnit 5 + Mockito with both success/failure cases per feature (Korean test method names are acceptable), but this isn't yet followed in practice. Existing tests must never be deleted per project rules.
