# 인증/인가 흐름

JWT 기반, Stateless 인증. Spring Security는 필터 체인/`PasswordEncoder`(BCrypt) 용도로만 사용하고, 세션·폼로그인·CSRF는 모두 비활성화되어 있습니다.

## 구성 요소

| 컴포넌트 | 역할 |
|---|---|
| `SecurityConfig` | 필터 체인 구성, 공개 경로 목록, 401/403 커스텀 응답 |
| `JwtAuthenticationFilter` | 요청마다 `Authorization` 헤더의 access token을 검증하고 `SecurityContext`에 인증 정보 설정 |
| `JwtTokenProvider` | JWT 생성/검증 (HMAC-SHA, `app.jwt.secret`) |
| `RefreshTokenService` | refresh token 발급/검증/폐기 (Redis 기반, JWT 아님) |
| `MemberAuthService` / `MemberAuthServiceImpl` | 회원가입/로그인/재발급/로그아웃 유스케이스 |

## Access Token

- 형식: JWT (HS256 계열, `jjwt` 라이브러리)
- 클레임: `iss`(issuer, `app.jwt.issuer` = `paceleague`), `sub`(memberSno), `memberId`, `type=access`, `iat`, `exp`
- 만료: `app.jwt.access-token-ttl-seconds` (로컬/운영 공통 300초 = 5분)
- 서버는 access token을 저장하지 않음(완전 stateless) → **폐기(블랙리스트) 불가**. 로그아웃해도 access token은 만료 시각까지 유효.

## Refresh Token

- 형식: **JWT가 아님**. `RandomStringUtils.randomAlphanumeric(64)`로 생성한 64자 랜덤 영숫자 문자열.
  - `JwtTokenProvider.createRefreshToken(...)` 메서드가 존재하지만 **실제로는 사용되지 않음** (미사용 코드).
- 저장: Redis, 키 `refresh:<token>` → 값 `memberSno`, TTL = `app.jwt.refresh-token-ttl-seconds` (로컬/운영 공통 1,209,600초 = 14일)
- **Rotation**: `/api/member/reissue` 호출 시 기존 refresh token은 즉시 삭제(`revoke`)되고 새 refresh token이 발급됨. 같은 refresh token으로 재발급을 두 번 시도하면 두 번째 요청은 실패.
- 로그아웃 시 Redis에서 해당 키를 삭제하는 것이 폐기의 전부.

## 인증된 요청 처리 순서

1. 클라이언트가 `Authorization: Bearer <accessToken>` 헤더로 요청.
2. `JwtAuthenticationFilter`(`UsernamePasswordAuthenticationFilter` 이전에 등록됨)가 헤더를 파싱.
3. `JwtTokenProvider.parseAndValidate(token)`으로 서명/issuer/만료 검증.
   - 검증 실패(서명 불일치, 만료, issuer 불일치 등) 시 예외를 잡아 `SecurityContext`를 비우고 그냥 필터 체인을 통과시킴(예외를 던지지 않음).
4. 검증 성공 시 `sub`(memberSno)와 `memberId` 클레임으로 `AuthPrincipal(memberSno, memberId)`를 만들고, `ROLE_USER` 권한 하나를 부여한 `Authentication`을 `SecurityContext`에 설정.
5. `SecurityConfig`의 `anyRequest().authenticated()` 규칙에 의해, 3~4단계에서 인증 정보가 설정되지 않은 상태로 보호된 경로에 도달하면 `authenticationEntryPoint`가 401을 응답.
6. 컨트롤러는 `((JwtAuthenticationFilter.AuthPrincipal) authentication.getPrincipal()).memberSno()`로 현재 로그인한 회원의 PK(`Member.sno`)를 꺼내 씀. 이 캐스팅 패턴은 `RecordController`, `RankController`, `RankingController`에 동일하게 반복되며, 별도의 `@AuthenticationPrincipal` 커스텀 리졸버나 애노테이션으로 추상화되어 있지 않음.

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
