# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

PaceLeague — a Spring Boot backend for a running-record and ranking service. Users log runs, the server computes pace/calories/score, and members are ranked by season/tier.

This is a **monorepo** with two independently-managed parts deployed together:

- **`api/`** — the Spring Boot backend (everything described below). Serves `api.paceleague.co.kr`.
- **`web/`** — `index.html` is the landing page **and** the community feed in one, reddit.com-style: the board feed (tabs/sort/post list) is visible to everyone, logged in or not; the header shows a login button when logged out or nickname/write/logout when logged in. Posting, commenting, and voting require login (redirect to `login.html` if attempted while logged out) — only reading is public. `post.html` (detail/comments/voting) and `login.html` are separate pages; all three share `js/app.js` for auth/fetch helpers and call the `board`/`member` APIs directly from the browser. Plus the ko/en privacy/terms/account-deletion pages required for app store compliance. Serves `paceleague.co.kr` / `www.paceleague.co.kr`. No build step, no framework — plain multi-page HTML/vanilla JS served directly by Nginx (deliberately kept this way even for the community feature — see `docs/architecture.md`).

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

**2026-08-10: converted to Clean Architecture (use-case + ports & adapters) at the user's explicit request** — see `docs/architecture.md` for the full rationale, since this is a deliberate, documented exception to `AGENTS.md`'s normal "no unrequested abstraction / no large-scale refactor" rules. Still a single Gradle module (no multi-module split).

Each domain package under `com.example.paceleague` follows: `domain/{entity,policy,enums}` (JPA entities used directly as domain models, not split into a separate framework-free model — see architecture.md for why), `application/port/{in,out}` + `application/{service,dto}` (use-case interfaces/impls, output ports have zero Spring Data/JPA imports), `adapter/{in/web, out/persistence}` (controllers depending only on `port/in`; `*JpaRepository` + `*PersistenceAdapter` implementing `port/out`). Only two things get ported: a domain's own JPA repository, and genuine cross-domain repository reach (e.g. `record` now depends on `season.GetCurrentSeasonPort` + `rank.ApplyScoreUseCase` instead of importing `rank`/`season` repositories directly). Generic infra clients (`StringRedisTemplate`, `TranslateClient`, `PasswordEncoder`) are injected directly into use-case impls, not portified. `common/` stays as shared-kernel config/error/response/security (plus a new `common/web` for the `@MemberSno` argument resolver — see Auth flow below); `JwtTokenProvider` lives in `common/security/jwt/`, not under `member`, since `JwtAuthenticationFilter`/`SecurityConfig` depend on it directly. See `AGENTS.md` for the full rule set this codebase is meant to follow (Java/Gradle only, no Kotlin/Maven, minimal abstraction beyond what's described above, no multi-module split — the `api/`+`web/` monorepo split is a deployment-unit split, not the kind of multi-module Gradle setup that rule is about).

Domains: `member` (auth), `record` (running logs), `rank` (individual score/tier), `ranking` (leaderboard), `season` (no controller — only exposes `GetCurrentSeasonPort` for other domains), `appversion` (mobile force/soft update check), `board` (community: boards/posts/comments/votes), `common` (cross-cutting config/response/error/web).

Note the `rank` vs `ranking` split — they are separate packages by design, not duplicates: `rank` answers "what's my score/tier" (`GET /api/rank/me`), `ranking` answers "show me the leaderboard" (`GET /api/ranking/getRanking`).

### Auth flow

JWT-based, stateless (`SecurityConfig`, `sessionCreationPolicy(STATELESS)`).

- Public endpoints: `/api/member/join`, `/login`, `/reissue`, `/logout`, `/api/app/version-check`, `/api/ranking/top10`, `/api/common/language`, Swagger paths, plus four `GET`-only `/api/board/**` reads (list boards, list posts, post detail, list comments — method-scoped via `HttpMethod.GET` matchers in `SecurityConfig`, since the same paths' `POST`/`DELETE` still require auth). Everything else requires a valid access token.
- CORS is otherwise closed. Four registrations exist in `common.config.CorsConfig`, all scoped to `paceleague.co.kr`/`www.paceleague.co.kr`: `/api/ranking/top10` (GET only, the original public-landing-page exception), `/api/member/**` and `/api/board/**` (GET/POST/DELETE + `Authorization`/`Content-Type` headers, added so `web/index.html`/`login.html`/`post.html` can call auth and community endpoints from the browser), and `/api/record/recent-30-days` (GET only + `Authorization` header, added so the board post-creation dialog can let a logged-in user pick one of their own recent runs to attach — see "Board post ↔ record attachment" below). A `local`-profile-only bean additionally allows `http://localhost:*` for dev. Don't widen this further without a reason — every other `record` endpoint, plus all of `rank`/`appversion`, has no CORS headers and can't be called from a browser on a different origin.
- `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`, validates the access token, and puts a `JwtAuthenticationFilter.AuthPrincipal(memberSno)` on the `Authentication`. Controllers pull the current user via a `@MemberSno Long memberSno` parameter (`common.web.MemberSnoArgumentResolver`, added in the 2026-08-10 Clean Architecture pass) — before that, every authenticated controller repeated its own `((AuthPrincipal) authentication.getPrincipal()).memberSno()` cast. `@MemberSno(required = false)` is used by `BoardController`'s public read endpoints to get `null` instead of an exception when unauthenticated.
- Access tokens are short-lived JWTs (`JwtTokenProvider`); refresh tokens are opaque random strings stored in Redis with TTL (`RefreshTokenService`, key prefix `refresh:`), not JWTs. `/reissue` rotates the refresh token (old one revoked, new one issued).
- Password hashing via `BCryptPasswordEncoder` (Spring Security is used only for auth mechanics here, not full MVC integration).

### Response/error conventions

- All controller responses wrap in `ResponseApi<T>` (`success`/`code`/`message`/`data`/`timestamp`) via `ResponseApi.success(...)`.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) maps `IllegalArgumentException` → 400 and everything else → 500, both as `ApiError` bodies with `ErrorCode`. Domain/service-layer validation failures should throw `IllegalArgumentException` to get a proper 400 rather than falling through to the generic 500 handler.

### Record calculations

`RecordSummaryCalculator` (in `record.domain.policy`) is the pace/calorie math: distance in meters → km, pace = duration / distance rounded HALF_UP, calories = weight(kg) × distance(km) (a rough linear approximation, not a real MET-based formula). `RecordScoreCalculator` (same package) holds the pure base-score/pace-bonus math extracted out of `RecordServiceImpl` during the Clean Architecture pass. `RankTierPolicy` (in `rank.domain.policy`) derives a `RankTier` from a raw score by finding the highest tier whose `minScore` the score clears.

Known gap: `RecordController.getMonthAll` hardcodes `weightKg = 70` instead of reading the member's actual weight (marked with a TODO in the code).

### Board post ↔ record attachment / author tier badge

`post.record_sno` (nullable, see `docs/migrations/2026-08-11_post_record_attachment.sql`) lets a post optionally reference one of the author's own `record` rows. `BoardServiceImpl.createPost` validates ownership at write time via `record.RecordQueryService.getOne(memberSno, recordSno)` (throws same as any other board 400 if not found/not owned) — no period restriction beyond whatever `getOne` itself enforces. `BoardQueryServiceImpl` resolves it back to a summary at read time via `record.GetRecordSummaryPort` (looked up by `recordSno` alone, no owner check, since any viewer can see a public post) and returns it as `PostDetailResponse.attachedRecord` (`null` if the post has none, or if the referenced record was later deleted — no FK enforced). `PostSummaryResponse.recordSno` exposes just the raw id for a list-view indicator, without the full summary. Both `PostSummaryResponse` and `PostDetailResponse` also carry `authorTier` (`rank.GetMemberTierPort`, current-season tier, defaults to `SILVER` if the author has no score yet this season) — the "author profile" shown next to posts is nickname + tier badge only, no profile picture (not modeled on `Member`).

### Static UI label i18n (board category names, tier badges)

2026-08-11: board category names and tier labels are translated server-side to match the web UI's language picker (`web/js/i18n.js`, 10 languages), instead of the old client-side hardcoded Korean `TIER_LABEL` map. Since both value sets are small and fixed (3 boards, 7 tiers) — unlike free-text post/comment translation, which goes through paid AWS Translate (`board.TranslationServiceImpl`) — this uses static lookup tables baked into the code: `common.i18n.Language` (enum mirroring the 10 web language codes, `fromCode` defaults to `KO` for null/unsupported), `rank.domain.policy.RankTierLabelPolicy` (7×10 table), `board.domain.policy.BoardLabelPolicy` (3 slugs × 9 languages, Korean always falls back to the DB value directly rather than duplicating it). All affected GET endpoints (`/api/board`, `/api/board/{boardSno}/posts`, `/api/board/posts/{postSno}`, `/api/rank/me`, `/api/ranking/getRanking`, `/api/ranking/top10`) take a `lang` query param (default `ko`); raw enum/code fields (`RankTier tier`/`authorTier`/`currentTier`) are kept alongside the new translated label fields (`tierLabel`/`authorTierLabel`/`currentTierLabel`) so any client needing the language-independent code (e.g. the mobile app) still has it. One documented exception to "cross-domain access only through ports": `BoardQueryServiceImpl` calls `rank.domain.policy.RankTierLabelPolicy` directly rather than through a port, since it's a stateless static lookup with no Spring bean or DB access — see `docs/architecture.md` for the rationale.

2026-08-11 (same day, follow-up): added `GET /api/common/language?country=KR` (public, `common.web.LocaleController`) so non-web clients (mobile app) can resolve a country code to one of the 10 `lang` codes above, via `common.i18n.CountryLanguageResolver` (ISO 3166-1 alpha-2 → `Language`, unmapped/blank defaults to `EN` — deliberately different from `Language.fromCode`'s `KO` default, since "unknown language string" and "unknown/unlisted country" warrant different fallbacks). This has no domain/port/service layering (no DB access at all) — just a static resolver called directly from the controller, same minimal-abstraction reasoning as the tier/board label policies above. `web/js/i18n.js` doesn't call it — the web frontend already resolves its own language client-side from `navigator.language`/localStorage.

## Testing

Only `PaceleagueApplicationTests` (context-load smoke test) currently exists — `AGENTS.md` calls for JUnit 5 + Mockito with both success/failure cases per feature (Korean test method names are acceptable), but this isn't yet followed in practice. Existing tests must never be deleted per project rules.

## Keeping docs in sync

`docs/` and this file are the source of truth for infra/architecture decisions that aren't obvious from the code (domain wiring, deploy pipeline, AWS layout, etc.). When you make a change that affects them — a new endpoint, a deploy pipeline change, a new domain/env var, infra topology — update the relevant `docs/*.md` file (and this file if it's a command/structure change) in the same change, not as a follow-up.
