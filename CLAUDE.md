# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

PaceLeague — a Spring Boot backend for a running-record and ranking service. Users log runs, the server computes pace/calories/score, and members are ranked by season/tier.

This is a **monorepo** with two independently-managed parts deployed together:

- **`api/`** — the Spring Boot backend (everything described below). Serves `api.paceleague.co.kr`.
- **`web/`** — a static marketing/legal site (landing page + ko/en privacy/terms/account-deletion pages required for app store compliance), plus a small community UI (`login.html`, `board.html`, `post.html`, sharing `js/app.js` for auth/fetch helpers) that calls the `board`/`member` APIs directly from the browser. Serves `paceleague.co.kr` / `www.paceleague.co.kr`. No build step, no framework — plain multi-page HTML/vanilla JS served directly by Nginx (deliberately kept this way even for the community feature — see `docs/architecture.md`).

A single push to `main` deploys both (see Deploy below) — there is no separate pipeline for `web/`.

## Commands

Run all Gradle commands from `api/` (or pass `-p api`):

```bash
cd api && ./gradlew bootRun          # run locally (Windows: gradlew.bat bootRun)
cd api && ./gradlew test             # run all tests (JUnit 5)
cd api && ./gradlew test --tests "com.example.paceleague.SomeTest"   # run a single test class
cd api && ./gradlew clean build      # full build (used by CI/Docker, run with -x test to skip tests)
```

Local run requires a `local` Spring profile with env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (see `api/src/main/resources/application-local.yml`), plus a running MySQL and Redis (Redis defaults to `127.0.0.1:6379`). `.env` lives at `api/.env` (gitignored) since that's the working directory `./gradlew bootRun` runs from.

## Deploy (single pipeline for api/ + web/)

`docker build -t paceleague .` is run from the **repo root** (build context spans both `api/` and `web/`) — the Dockerfile builds the `api/` jar in a builder stage, then in the final image also `COPY web /web-dist` so the static site travels inside the same image as a distribution artifact (the Spring Boot app does not serve it).

GitHub Actions deploys on push to `main` (`.github/workflows/deploy.yml`): builds the jar from `api/`, builds/pushes the Docker image to AWS ECR, then triggers `.github/ssm-commands.json` on EC2 via SSM, which — in this order, deliberately, so a web-asset-extraction failure can never delay or block the app coming back up:
1. pulls the new image and **restarts the `paceleague` app container first** (serves `api.paceleague.co.kr` via Nginx reverse proxy on port 8080) — this is the priority; the app must not stay down
2. only then extracts `/web-dist` from the same image into a temp dir and swaps it into `/var/www/paceleague` (what Nginx serves for `paceleague.co.kr`), guarded so a failed extraction leaves the previous static site in place rather than deleting it

So `api.paceleague.co.kr` and `paceleague.co.kr` both update together from one push — see `docs/infra.md` for the full Nginx/domain wiring on the EC2 host.

Local Docker smoke test: `docker build -t paceleague .` / `docker run -d -p 8080:8080 --name paceleague paceleague` (run from repo root, not `api/`).

## Architecture

Each domain package under `com.example.paceleague` follows a strict layered structure: `controller → service → repository`, with `entity`/`dto` alongside. Controllers must stay thin (no business logic, no `@Transactional`); services own transactions and business logic; repositories are the only DB access point. See `AGENTS.md` for the full rule set this codebase is meant to follow (Java/Gradle only, no Kotlin/Maven, minimal abstraction, no unrequested design patterns or multi-module split — the `api/`+`web/` monorepo split is a deployment-unit split, not the kind of multi-module Gradle setup that rule is about).

Domains: `member` (auth), `record` (running logs), `rank` (individual score/tier), `ranking` (leaderboard), `season`, `appversion` (mobile force/soft update check), `board` (community: boards/posts/comments/votes), `common` (cross-cutting config/response/error).

Note the `rank` vs `ranking` split — they are separate packages by design, not duplicates: `rank` answers "what's my score/tier" (`GET /api/rank/me`), `ranking` answers "show me the leaderboard" (`GET /api/ranking/getRanking`).

### Auth flow

JWT-based, stateless (`SecurityConfig`, `sessionCreationPolicy(STATELESS)`).

- Public endpoints: `/api/member/join`, `/login`, `/reissue`, `/logout`, `/api/app/version-check`, `/api/ranking/top10`, Swagger paths. Everything else — including all of `/api/board/**` — requires a valid access token; there is no anonymous read access to the community feature.
- CORS is otherwise closed. Three registrations exist in `common.config.CorsConfig`, all scoped to `paceleague.co.kr`/`www.paceleague.co.kr`: `/api/ranking/top10` (GET only, the original public-landing-page exception), `/api/member/**` and `/api/board/**` (GET/POST/DELETE + `Authorization`/`Content-Type` headers, added so `web/login.html`/`board.html`/`post.html` can call auth and community endpoints from the browser). A `local`-profile-only bean additionally allows `http://localhost:*` for dev. Don't widen this further without a reason — every other endpoint (`record`, `rank`, `appversion`) has no CORS headers and can't be called from a browser on a different origin.
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

Only `PaceleagueApplicationTests` (context-load smoke test) currently exists — `AGENTS.md` calls for JUnit 5 + Mockito with both success/failure cases per feature (Korean test method names are acceptable), but this isn't yet followed in practice. Existing tests must never be deleted per project rules.

## Keeping docs in sync

`docs/` and this file are the source of truth for infra/architecture decisions that aren't obvious from the code (domain wiring, deploy pipeline, AWS layout, etc.). When you make a change that affects them — a new endpoint, a deploy pipeline change, a new domain/env var, infra topology — update the relevant `docs/*.md` file (and this file if it's a command/structure change) in the same change, not as a follow-up.
