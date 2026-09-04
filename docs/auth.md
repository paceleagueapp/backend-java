# 인증/인가 흐름

JWT 기반, Stateless 인증. Spring Security는 필터 체인/`PasswordEncoder`(BCrypt) 용도로만 사용하고, 세션·폼로그인·CSRF는 모두 비활성화되어 있습니다.

## 구성 요소

| 컴포넌트 | 역할 |
|---|---|
| `common.config.SecurityConfig` | 필터 체인 구성, 공개 경로 목록, 401/403 커스텀 응답 |
| `common.security.JwtAuthenticationFilter` | 요청마다 `Authorization` 헤더의 access token을 검증하고 `SecurityContext`에 인증 정보 설정 |
| `common.security.jwt.JwtTokenProvider` | JWT 생성/검증 (HMAC-SHA, `app.jwt.secret`) — 2026-08-10 클린 아키텍처 전환 시 `member` 패키지에서 `common`으로 재배치([architecture.md](./architecture.md) 참고) |
| `member.adapter.out.token.RedisRefreshTokenAdapter` | refresh token 발급/검증/폐기 (Redis 기반, JWT 아님) — `member.application.port.out.RefreshTokenStorePort` 구현체 |
| `member.application.port.in.MemberAuthUseCase` / `member.application.service.MemberAuthService` | 회원가입/로그인/재발급/로그아웃 유스케이스 |

## Access Token

- 형식: JWT (HS256 계열, `jjwt` 라이브러리)
- 클레임: `iss`(issuer, `app.jwt.issuer` = `paceleague`), `sub`(memberSno), `memberId`, `type=access`, `iat`, `exp`
- 만료: `app.jwt.access-token-ttl-seconds` (로컬/운영 공통 300초 = 5분)
- 서버는 access token을 저장하지 않음(완전 stateless) → **폐기(블랙리스트) 불가**. 로그아웃해도 access token은 만료 시각까지 유효.

## Refresh Token

- 형식: **JWT가 아님**. `RandomStringUtils.randomAlphanumeric(64)`로 생성한 64자 랜덤 영숫자 문자열.
  - 예전엔 `JwtTokenProvider.createRefreshToken(...)`이라는 미사용 메서드가 있었으나, 2026-08-10 클린 아키텍처 전환 시 호출자 0건 확인 후 삭제됨.
- 저장: Redis, 키 `refresh:<token>` → 값 `memberSno`, TTL = `app.jwt.refresh-token-ttl-seconds` (로컬/운영 공통 1,209,600초 = 14일)
- **Rotation**: `/api/member/reissue` 호출 시 `RefreshTokenStorePort.validateAndRevoke(...)`가 Redis `GETDEL`로 조회+삭제를 원자적으로 처리한 뒤 새 refresh token을 발급함. 같은 refresh token으로 동시에 두 번 재발급을 시도해도 하나만 성공(과거엔 조회/삭제가 분리된 두 단계라 동시 요청 시 둘 다 통과할 수 있는 경쟁 조건이 있었음 — 이번 클린 아키텍처 전환과는 별개로 이미 원자화로 수정되어 있던 상태를 그대로 이전함).
- 로그아웃 시 Redis에서 해당 키를 삭제하는 것이 폐기의 전부.

## 인증된 요청 처리 순서

1. 클라이언트가 `Authorization: Bearer <accessToken>` 헤더로 요청.
2. `JwtAuthenticationFilter`(`UsernamePasswordAuthenticationFilter` 이전에 등록됨)가 헤더를 파싱.
3. `JwtTokenProvider.parseAndValidate(token)`으로 서명/issuer/만료 검증.
   - 검증 실패(서명 불일치, 만료, issuer 불일치 등) 시 예외를 잡아 `SecurityContext`를 비우고 그냥 필터 체인을 통과시킴(예외를 던지지 않음).
4. 검증 성공 시 `sub`(memberSno)와 `memberId` 클레임으로 `AuthPrincipal(memberSno, memberId)`를 만들고, `ROLE_USER` 권한 하나를 부여한 `Authentication`을 `SecurityContext`에 설정.
5. `SecurityConfig`의 `anyRequest().authenticated()` 규칙에 의해, 3~4단계에서 인증 정보가 설정되지 않은 상태로 보호된 경로에 도달하면 `authenticationEntryPoint`가 401을 응답.
6. 컨트롤러는 `@MemberSno Long memberSno` 파라미터로 현재 로그인한 회원의 PK(`Member.sno`)를 꺼내 씀 — 내부적으로 `common.web.MemberSnoArgumentResolver`가 `((JwtAuthenticationFilter.AuthPrincipal) authentication.getPrincipal()).memberSno()` 캐스팅을 대신 수행한다. `RecordController`, `RankController`, `RankingController`, `BoardController` 전체가 이 하나의 리졸버를 공유한다(2026-08-10 클린 아키텍처 전환 전에는 컨트롤러마다 `uno(authentication)` 헬퍼가 반복됐음 — [architecture.md](./architecture.md) 참고). `BoardController`의 비로그인-허용 조회 엔드포인트는 `@MemberSno(required = false)`를 써서 인증 정보가 없으면 예외 대신 `null`을 받는다.

## 플로우 다이어그램 (텍스트)

```text
[회원가입/로그인]
Client → POST /api/member/join 또는 /login
       ← { accessToken, refreshToken, ... }  (access 5분 / refresh 14일)

[인증 필요 API 호출]
Client → GET /api/record/... (Authorization: Bearer <accessToken>)
       ← 200 (정상) 또는 401 (토큰 없음/만료/무효)

[access token 만료 시]
Client → POST /api/member/reissue { refreshToken }
       ← 새 accessToken + 새 refreshToken (기존 refreshToken은 폐기됨)

[로그아웃]
Client → POST /api/member/logout { refreshToken }
       ← refreshToken이 Redis에서 삭제됨 (access token은 만료 전까지 여전히 유효)
```

## 비밀번호

- `BCryptPasswordEncoder`로 해시 저장 (`Member.passwordHash`).
- 로그인 실패 시 "아이디를 못 찾음"과 "비밀번호 불일치"를 구분하지 않고 동일한 메시지(`"아이디 또는 비밀번호가 올바르지 않습니다."`)로 응답 (계정 존재 여부 노출 방지).

## 시크릿 관리

- `app.jwt.secret`은 환경 변수 `JWT_SECRET`에서 주입 (`application-local.yml` / `application-prod.yml`), 코드/설정 파일에 하드코딩되어 있지 않음.
- `.env` 파일과 운영용 설정은 Git에 커밋하지 않는 것이 원칙 (`README.md`, `AGENTS.md` 명시).

## CORS

기본적으로 CORS는 열려 있지 않습니다 — 브라우저에서 다른 오리진(예: `web/`이 서빙되는 `paceleague.co.kr`)이 이 API를 `fetch`로 호출하면 차단됩니다. 유일한 예외는 `GET /api/ranking/top10`이며, `common.config.CorsConfig`가 이 경로 하나에만 `https://paceleague.co.kr`, `https://www.paceleague.co.kr` 오리진의 GET 요청을 허용합니다 ([api.md](./api.md#get-apirankingtop10--상위-10명-랭킹-조회-공개-인증-불필요) 참고). 다른 공개 API(`/api/app/version-check` 등)는 서버-투-서버 또는 앱 클라이언트 호출만 가정하므로 CORS를 열지 않았습니다.
