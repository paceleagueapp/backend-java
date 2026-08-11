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
| 캐시 | Redis (Spring Data Redis, `StringRedisTemplate`) — refresh token 저장 + board 번역 결과 캐싱(180일) |
| 번역 | AWS Translate (`software.amazon.awssdk:translate` 2.46.7) — board 게시글/댓글 번역, 자격증명은 EC2 인스턴스 프로필(`paceleague-s3-read` role) 기본 체인 사용, `common.config.AwsTranslateConfig` |
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
  → Controller (adapter/in/web — 요청 파싱만, 로직 없음, port/in에만 의존)
  → UseCase 구현체 (application/service — @Transactional, 비즈니스 로직, port/out에만 의존)
  → PersistenceAdapter (adapter/out/persistence — port/out 구현, 내부적으로 Spring Data JpaRepository 위임)
  → MySQL / Redis
```

모든 컨트롤러 응답은 `ResponseApi<T>`로 감싸고, 예외는 `GlobalExceptionHandler`(`@RestControllerAdvice`)가 `ApiError`로 변환합니다. 자세한 내용은 [api.md](./api.md) 참고.

## 패키지 구조 (클린 아키텍처 — 유스케이스 + 포트&어댑터)

**2026-08-10, 사용자의 명시적 요청으로 단순 Layered Architecture(`controller/service/repository/entity/dto`)에서 클린 아키텍처로 전환했습니다.** `AGENTS.md`의 "과도한 추상화 금지"/"대규모 리팩토링 임의 진행 금지" 규칙과 원래는 충돌하는 결정이지만, 이번 건은 명시적으로 승인된 예외이고 `AGENTS.md`에도 그렇게 기록해뒀습니다. 멀티모듈 Gradle 분리는 하지 않았습니다 — 여전히 단일 `api` 모듈이고, 계층은 패키지 구조로만 나뉩니다.

각 도메인 패키지는 아래 구조를 기본으로 따릅니다:

```text
{domain}/
  domain/
    entity/         JPA @Entity 그대로 도메인 모델로 사용 (프레임워크 독립적인 순수 POJO로 이중화하지 않음 — 아래 "왜 엔티티를 분리하지 않았는가" 참고)
    policy/ enums/   순수 비즈니스 규칙 (RankTierPolicy, RecordScoreCalculator 등)
  application/
    port/in/         유스케이스 인터페이스 — 기존 서비스 인터페이스가 있던 도메인(member/record/board)은 이름을 그대로 유지, 없던 도메인(rank/ranking/appversion)은 새로 추출
    port/out/         리포지토리 모양의 출력 포트 — Spring Data/JPA import 없음, 실제 쓰는 메서드만 선언
    service/          유스케이스 구현체 (기존 *ServiceImpl 위치, 이제 port/out에만 의존)
    dto/              Command/Result 경계 타입 (기존 dto 그대로 이동)
  adapter/
    in/web/           컨트롤러 (port/in에만 의존)
    out/persistence/  Spring Data JpaRepository(`*JpaRepository`, 내부 구현 디테일) + port/out을 구현하는 얇은 어댑터(`*PersistenceAdapter`)
```

포트화하는 대상은 딱 두 가지입니다: **(1)** 그 도메인 자신의 JPA 리포지토리, **(2)** 다른 도메인 저장소로의 실제 크로스 도메인 접근(예: `record`가 `rank`/`season`을 직접 찌르던 것, `board`가 `member`를 직접 찌르던 것). `StringRedisTemplate`, `TranslateClient`, `PasswordEncoder`, `JwtProperties` 같은 범용 인프라 클라이언트는 포트로 감싸지 않고 유스케이스 구현체에 그대로 주입합니다 — 여기까지 포트화하면 요청받지 않은 과도한 추상화이기 때문입니다(`MemberAuthServiceImpl`의 로그인 잠금 Redis 코드, `TranslationServiceImpl`의 AWS Translate/Redis 캐싱 코드가 대표적인 예).

```text
com.example.paceleague
├── member       회원 가입/로그인/토큰 재발급/로그아웃 (인증 도메인)
├── record       러닝 기록 저장/조회, 기록 기반 점수 산출
├── rank         "내 점수/티어" 조회 (개인 관점)
├── ranking      리더보드(랭킹) 조회 (전체/주변 순위 관점) — domain/ 없음, MemberScore는 rank 소유
├── season       시즌 정보 (시작/종료일, 현재 시즌 조회) — 컨트롤러 없음, GetCurrentSeasonPort만 다른 도메인에 노출
├── appversion   모바일 앱 강제/선택 업데이트, 점검 여부 체크
├── board        커뮤니티(보드/게시글/댓글/추천) — record 도메인과 동일하게 query/write 유스케이스 분리
└── common       횡단 관심사: 설정, 응답 포맷, 에러 처리, JWT 필터. 위 도메인별 구조에 억지로 끼워맞추지 않고 지금 형태를 유지.
    ├── config       SecurityConfig, JwtConfig/JwtProperties, RedisConfig, JpaConfig, OpenApiConfig, WebMvcConfig
    ├── security     JwtAuthenticationFilter (+ AuthPrincipal), security/jwt/JwtTokenProvider
    ├── web          MemberSno(애너테이션), MemberSnoArgumentResolver — 인증 컨트롤러 공통 파라미터 리졸버
    ├── response     ResponseApi<T>
    └── error        ApiError, ErrorCode, GlobalExceptionHandler
```

`JwtTokenProvider`가 `member` 도메인이 아니라 `common/security/jwt/`에 있는 이유: `JwtAuthenticationFilter`(모든 도메인 요청이 거치는 필터)와 `SecurityConfig`가 이미 직접 의존하고 있어서, `member`의 adapter 밑에 두면 "공유 커널인 `common`이 특정 도메인의 어댑터에 의존"하는 역방향 의존이 생깁니다. JWT 서명/검증은 특정 도메인의 유스케이스가 아니라 순수 기술적 관심사이므로 `common`이 맞는 자리입니다. 반면 refresh token 발급/검증/폐기(`member/adapter/out/token/RedisRefreshTokenAdapter`, `RefreshTokenStorePort`)는 `member` 도메인의 유스케이스(로그인/재발급/로그아웃)를 위해서만 존재하므로 `member` 안에 남아있습니다.

### 도메인 간 의존 — 포트로만 넘나든다

이번 전환 전에는 `record.RecordServiceImpl`이 `rank`/`season`의 리포지토리를 직접 import해서 세 바운디드 컨텍스트가 한 클래스에 뒤섞여 있었고, `board.BoardQueryServiceImpl`도 닉네임 조회를 위해 `member`의 리포지토리를 직접 import했습니다. 지금은:

- `record` → `season.application.port.in.GetCurrentSeasonPort`(현재 시즌 조회), `rank.application.port.in.ApplyScoreUseCase`(점수 반영 — 예전 `RecordServiceImpl.saveRank`/`applyScoreToSeason` 로직이 통째로 `rank` 도메인 소유로 이전됨)에만 의존.
- `board` → `member.application.port.in.GetMemberNicknamePort`(작성자 닉네임), `rank.application.port.in.GetMemberTierPort`(작성자 티어뱃지), `record.application.port.in.RecordQueryService`(게시글 작성 시 첨부한 기록이 본인 소유인지 검증 — `getOne`은 원래 `record` 자신의 use-case지만 board가 그대로 재사용), `record.application.port.in.GetRecordSummaryPort`(게시글 조회 시 첨부 기록 요약 표시, 작성자가 아닌 제3자가 봐도 되도록 memberSno 소유권 검사 없이 recordSno만으로 조회)에 의존. 2026-08-11 "게시글에 러닝기록 첨부 + 작성자 프로필(티어)" 기능 추가 시 도입.
- `rank`/`ranking` → `season.application.port.in.GetCurrentSeasonPort`에만 의존.

`record`가 `ApplyScoreUseCase.applyScore(...)`를 자신의 `@Transactional` 메서드 안에서 호출하는데, 둘 다 스프링이 관리하는 별개 빈이라 기본 `REQUIRED` 전파로 호출자의 트랜잭션에 합류합니다 — 기록 저장 + 점수 로그 저장 + 시즌 누적 점수 갱신이 예전과 동일하게 하나의 트랜잭션으로 묶입니다.

### 왜 엔티티를 순수 도메인 객체로 분리하지 않았는가

"진짜" 클린 아키텍처는 JPA `@Entity`와 프레임워크 독립적인 도메인 모델을 완전히 분리하고 그 사이를 매퍼로 연결하지만, 이 프로젝트는 그렇게 하지 않기로 결정했습니다. 이유:

- 엔티티 11개 중 다수(`MemberScore.addScore`, `Rank`의 `@PreUpdate` 등)가 이미 실질적인 도메인 동작을 갖고 있어 완전한 빈혈 모델이 아닙니다 — 분리해도 그 동작을 어딘가로 옮기고 매퍼를 추가하는 비용만 늘어날 뿐, 단일 모듈에서 영속 기술을 바꿀 계획도 없어 실익이 없습니다.
- `Member.sno`가 `Integer`인데 리포지토리는 `Long`을 쓰는 기존 타입 불일치, `season.getSeason()`/`season.getSno()`를 서로 다르게 쓰는 `rank`/`ranking`의 불일치 같은 **기존 결함을 이번 리팩토링에서 그대로 보존**해야 했는데, 엔티티를 이중화하면 두 곳에 결함을 전파하거나 리팩토링 도중 몰래 "고쳐버리는" 위험이 커집니다.

### `rank` vs `ranking` — 왜 나뉘어 있는가

이름이 비슷하지만 **의도적으로 분리된 별개 패키지**입니다.

- `rank`: "내 점수/티어가 뭐야?" → `GET /api/rank/me`
- `ranking`: "리더보드 보여줘" → `GET /api/ranking/getRanking`

둘 다 `rank.domain.entity.MemberScore`(시즌별 누적 점수/티어)를 읽지만, `rank`는 본인 1건만 조회하고 `ranking`은 Top3 + 내 주변 순위를 네이티브 쿼리로 집계합니다.

## 레이어 규칙

- **Controller (`adapter/in/web`)**: 요청/응답 매핑만 담당. 비즈니스 로직, `@Transactional` 금지. `port/in` 유스케이스 인터페이스에만 의존하고 구현체(`*ServiceImpl`)를 직접 주입받지 않는다.
- **UseCase 구현체 (`application/service`)**: 비즈니스 로직과 트랜잭션 경계를 소유. 조회는 `@Transactional(readOnly = true)`, 변경은 `@Transactional`. `port/out`에만 의존하고 Spring Data `JpaRepository`를 직접 주입받지 않는다.
- **PersistenceAdapter (`adapter/out/persistence`)**: DB 접근은 오직 이 계층에서만. 단순 CRUD는 내부 `*JpaRepository`의 `JpaRepository` 메서드 이름 규칙 사용, 복잡한 집계는 네이티브 쿼리(`@Query(nativeQuery = true)`) 사용 (예: `RankingJpaRepository`, `RecordJpaRepository`의 요약 집계). 어댑터 클래스 자체는 `port/out` 인터페이스를 구현만 하고 위임할 뿐 로직을 갖지 않는다.
- **DTO (`application/dto`)**: Entity를 API 응답으로 직접 반환하지 않는다. 모든 응답 DTO는 엔티티 필드를 그대로 담지 않고 명시적으로 값을 옮겨 담는다(`RecordResponse.from(Record)` 같은 정적 팩토리 메서드 패턴).

## 클린 코드 원칙

`AGENTS.md`의 "과도한 추상화 금지"·"메서드는 짧고 명확하게" 규칙은, 위 포트&어댑터 구조 **안에서** 아래 방식으로 지켜집니다 — 유스케이스/포트 단위를 넘어서는 추가 쪼개기(예: 유스케이스를 메서드 단위로 더 잘게 나누기)는 여전히 금지.

- **DTO는 엔티티를 감싸지 않는다**: 응답 DTO 필드는 엔티티 타입이 아닌 원시/값 타입만 사용한다. 타입이 불확실하다고 `Object`로 남겨두지 않는다 — 엔티티의 실제 컬럼 타입을 그대로 명시한다.
- **중복 로직은 private 메서드로 추출한다**: 같은 유스케이스 구현체 안에서 2회 이상 반복되는 로직(예: 토큰 발급, 기록 저장+점수 산정)은 새 클래스를 만들지 않고 private 메서드로 뽑아낸다.
- **긴 메서드는 의미 단위로 분리한다**: 여러 계산 단계가 섞인 메서드(예: 점수 산정)는 각 단계를 이름이 있는 private 메서드로 나눠, 메서드 이름 자체가 문서 역할을 하도록 한다. `RecordServiceImpl`의 `computeAndApplyScore`가 순수 계산 부분(`RecordScoreCalculator.calculateBaseScore`/`calculatePaceBonus`)과 리포지토리 접근이 필요한 `calculateWeeklyBonus`로 나뉘고, 최종 반영은 `rank.ApplyScoreUseCase`에 위임하는 것이 예시이며, 각 단계는 [domains.md](./domains.md)의 점수 산정 문서와 1:1로 대응한다.
- **인증된 컨트롤러의 memberSno 추출은 공유 리졸버로 통합했다**: 클린 아키텍처 전환 전에는 `uno(authentication)` 캐스팅 헬퍼가 컨트롤러마다 반복됐지만, 지금은 `@MemberSno Long memberSno`(`common.web.MemberSnoArgumentResolver`) 하나로 통일했다 — 이건 "새 추상화 계층"이 아니라 유스케이스 포트 도입과 함께 자연스럽게 정리된 것이며, 이 이상으로 공통 유틸리티를 늘리지는 않는다.

## 인증 전체 구조

세부 흐름은 [auth.md](./auth.md) 참고. 핵심 컴포넌트:

- `common.config.SecurityConfig` — 필터 체인, 공개 엔드포인트 목록, 401/403 핸들러를 정의. `sessionCreationPolicy(STATELESS)`.
- `common.security.JwtAuthenticationFilter` — `UsernamePasswordAuthenticationFilter` 이전에 실행. `Authorization: Bearer <token>` 헤더를 검증하고 `AuthPrincipal(memberSno, memberId)`를 `SecurityContext`에 설정.
- `common.security.jwt.JwtTokenProvider` — access token 발급/검증. HMAC 서명(`app.jwt.secret`). 클린 아키텍처 전환 시 `member` 패키지에서 `common`으로 재배치됨(위 "패키지 구조" 참고), 사용되지 않던 `createRefreshToken`은 이때 삭제됨.
- `member.adapter.out.token.RedisRefreshTokenAdapter`(`member.application.port.out.RefreshTokenStorePort` 구현) — refresh token은 JWT가 아니라 64자 랜덤 문자열이며 Redis에 `refresh:<token>` 키로 저장(TTL은 `app.jwt.refresh-token-ttl-seconds`).

## 알려진 기술 부채 / TODO

- `RecordController.getMonthAll`: 실제 회원 체중을 조회하지 않고 `weightKg = 70`으로 하드코딩되어 있음 (칼로리 계산에 사용됨, 코드에 TODO 명시). `Member` 엔티티에 체중 컬럼 자체가 없어서, 해소하려면 DB 스키마 변경(운영은 `ddl-auto: validate`라 수동 마이그레이션 필요)과 회원가입/API 계약 변경이 함께 필요한 별도 작업입니다.
- 테스트는 `PaceleagueApplicationTests`(컨텍스트 로드 스모크 테스트) 하나뿐. `AGENTS.md`는 JUnit5 + Mockito로 성공/실패 케이스를 작성하도록 규정하지만 아직 실제로 지켜지지 않음.
- README에 언급된 QueryDSL은 `build.gradle` 의존성에 없음 (아직 도입되지 않음, 복잡한 집계는 현재 네이티브 SQL로 처리).
