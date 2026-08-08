# 아키텍처

이 문서는 `api/`(Spring Boot 백엔드) 내부 아키텍처를 다룹니다. 저장소 최상위 구조는 `api/`(백엔드)와 `web/`(정적 랜딩/약관 사이트) 모노레포이며, 배포 시 함께 묶여 나갑니다 — 자세한 배포/도메인 라우팅은 [setup.md](./setup.md), [infra.md](./infra.md) 참고. 아래 내용은 전부 `api/` 하위 코드 기준입니다.

## 기술 스택

| 영역 | 기술 |
|---|---|
| 언어/런타임 | Java 21 |
| 프레임워크 | Spring Boot 3.5.11 |
| 빌드 | Gradle (Maven/멀티모듈 미사용) |
| 인증 | Spring Security (인증 메커니즘 용도, 필터 체인 전체를 쓰지는 않음) + JWT (jjwt 0.12.6) |
| DB | MySQL (Spring Data JPA / Hibernate) |
| 캐시 | Redis (Spring Data Redis, `StringRedisTemplate`) — refresh token 저장 전용 |
| API 문서 | springdoc-openapi (Swagger UI) |
| 비밀번호 해시 | BCrypt |
| 기타 | Lombok, commons-lang3, spring-dotenv(.env 로딩) |
| 배포 | Docker → AWS ECR → AWS EC2 (SSM RunShellScript) via GitHub Actions |

## 요청 흐름

```text
Client
  → Nginx (운영)
  → Spring Boot (DispatcherServlet)
  → JwtAuthenticationFilter (Authorization 헤더 파싱, SecurityContext 설정)
  → Controller (요청 파싱만, 로직 없음)
  → Service (@Transactional, 비즈니스 로직)
  → Repository (JPA / Spring Data, DB 접근 유일 지점)
  → MySQL / Redis
```

모든 컨트롤러 응답은 `ResponseApi<T>`로 감싸고, 예외는 `GlobalExceptionHandler`(`@RestControllerAdvice`)가 `ApiError`로 변환합니다. 자세한 내용은 [api.md](./api.md) 참고.

## 패키지 구조 (도메인별 계층 구조)

각 도메인 패키지는 `controller / service / repository / entity / dto` 구조를 기본으로 따릅니다 (`AGENTS.md` 규칙).

```text
com.example.paceleague
├── member       회원 가입/로그인/토큰 재발급/로그아웃 (인증 도메인)
├── record       러닝 기록 저장/조회, 기록 기반 점수 산출
├── rank         "내 점수/티어" 조회 (개인 관점)
├── ranking      리더보드(랭킹) 조회 (전체/주변 순위 관점)
├── season       시즌 정보 (시작/종료일, 현재 시즌 조회)
├── appversion   모바일 앱 강제/선택 업데이트, 점검 여부 체크
├── board        커뮤니티(보드/게시글/댓글/추천) — record 도메인과 동일하게 query/write 서비스 분리
└── common       횡단 관심사: 설정, 응답 포맷, 에러 처리, JWT 필터
    ├── config       SecurityConfig, JwtConfig/JwtProperties, RedisConfig, JpaConfig, OpenApiConfig
    ├── security     JwtAuthenticationFilter (+ AuthPrincipal)
    ├── response     ResponseApi<T>
    └── error        ApiError, ErrorCode, GlobalExceptionHandler
```

### `rank` vs `ranking` — 왜 나뉘어 있는가

이름이 비슷하지만 **의도적으로 분리된 별개 패키지**입니다.

- `rank`: "내 점수/티어가 뭐야?" → `GET /api/rank/me`
- `ranking`: "리더보드 보여줘" → `GET /api/ranking/getRanking`

둘 다 `rank.entity.MemberScore`(시즌별 누적 점수/티어)를 읽지만, `rank`는 본인 1건만 조회하고 `ranking`은 Top3 + 내 주변 순위를 네이티브 쿼리로 집계합니다.

## 레이어 규칙

- **Controller**: 요청/응답 매핑만 담당. 비즈니스 로직, `@Transactional` 금지.
- **Service**: 비즈니스 로직과 트랜잭션 경계를 소유. 조회는 `@Transactional(readOnly = true)`, 변경은 `@Transactional`.
- **Repository**: DB 접근은 오직 이 계층에서만. 단순 CRUD는 `JpaRepository` 메서드 이름 규칙 사용, 복잡한 집계는 네이티브 쿼리(`@Query(nativeQuery = true)`) 사용 (예: `RankingRepository`, `RecordRepository`의 요약 집계).
- **DTO**: Entity를 API 응답으로 직접 반환하지 않는다. 모든 응답 DTO는 엔티티 필드를 그대로 담지 않고 명시적으로 값을 옮겨 담는다(`RecordResponse.from(Record)` 같은 정적 팩토리 메서드 패턴).

## 클린 코드 원칙

`AGENTS.md`의 "과도한 추상화 금지"·"메서드는 짧고 명확하게" 규칙을 실제 코드에 적용할 때는 새 클래스/인터페이스를 늘리지 않고 **같은 클래스 안에서** 아래 방식으로 정리합니다.

- **DTO는 엔티티를 감싸지 않는다**: 응답 DTO 필드는 엔티티 타입이 아닌 원시/값 타입만 사용한다. 타입이 불확실하다고 `Object`로 남겨두지 않는다 — 엔티티의 실제 컬럼 타입을 그대로 명시한다.
- **중복 로직은 private 메서드로 추출한다**: 같은 클래스 안에서 2회 이상 반복되는 로직(예: 토큰 발급, 기록 저장+점수 산정)은 새 클래스를 만들지 않고 private 메서드로 뽑아낸다.
- **긴 메서드는 의미 단위로 분리한다**: 여러 계산 단계가 섞인 메서드(예: 점수 산정)는 각 단계를 이름이 있는 private 메서드로 나눠, 메서드 이름 자체가 문서 역할을 하도록 한다. `RecordServiceImpl.saveRank`가 `calculateBaseScore`/`calculatePaceBonus`/`calculateWeeklyBonus`/`applyScoreToSeason`으로 분리된 것이 예시이며, 각 단계는 [domains.md](./domains.md)의 점수 산정 문서와 1:1로 대응한다.
- **컨트롤러 반복 패턴은 의도적으로 유지한다**: `uno(authentication)` 헬퍼가 인증된 컨트롤러마다 반복되는 것은 중복이 아니라 의도된 스타일(공유 리졸버/애너테이션을 두지 않음, 위 인증 전체 구조 참고)이므로 별도 추상화로 통합하지 않는다.

## 인증 전체 구조

세부 흐름은 [auth.md](./auth.md) 참고. 핵심 컴포넌트:

- `SecurityConfig` — 필터 체인, 공개 엔드포인트 목록, 401/403 핸들러를 정의. `sessionCreationPolicy(STATELESS)`.
- `JwtAuthenticationFilter` — `UsernamePasswordAuthenticationFilter` 이전에 실행. `Authorization: Bearer <token>` 헤더를 검증하고 `AuthPrincipal(memberSno, memberId)`를 `SecurityContext`에 설정.
- `JwtTokenProvider` — access token(및 미사용 상태의 `createRefreshToken`) 발급/검증. HMAC 서명(`app.jwt.secret`).
- `RefreshTokenService` — refresh token은 JWT가 아니라 64자 랜덤 문자열이며 Redis에 `refresh:<token>` 키로 저장(TTL은 `app.jwt.refresh-token-ttl-seconds`).

## 알려진 기술 부채 / TODO

- `RecordController.getMonthAll`: 실제 회원 체중을 조회하지 않고 `weightKg = 70`으로 하드코딩되어 있음 (칼로리 계산에 사용됨, 코드에 TODO 명시). `Member` 엔티티에 체중 컬럼 자체가 없어서, 해소하려면 DB 스키마 변경(운영은 `ddl-auto: validate`라 수동 마이그레이션 필요)과 회원가입/API 계약 변경이 함께 필요한 별도 작업입니다.
- 테스트는 `PaceleagueApplicationTests`(컨텍스트 로드 스모크 테스트) 하나뿐. `AGENTS.md`는 JUnit5 + Mockito로 성공/실패 케이스를 작성하도록 규정하지만 아직 실제로 지켜지지 않음.
- README에 언급된 QueryDSL은 `build.gradle` 의존성에 없음 (아직 도입되지 않음, 복잡한 집계는 현재 네이티브 SQL로 처리).
