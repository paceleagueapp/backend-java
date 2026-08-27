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
├── record       러닝 기록 저장/조회, 기록 기반 점수 산출, GPS 청크 누적(record_track) + 유휴 세션 스위퍼(adapter/in/scheduler)
├── rank         "내 점수/티어" 조회 (개인 관점)
├── ranking      리더보드(랭킹) 조회 (전체/주변 순위 관점) — domain/ 없음, MemberScore는 rank 소유
├── season       시즌 정보 (시작/종료일, 현재 시즌 조회) — 컨트롤러 없음, GetCurrentSeasonPort만 다른 도메인에 노출
├── appversion   모바일 앱 강제/선택 업데이트, 점검 여부 체크
├── board        커뮤니티(보드/게시글/댓글/추천) — record 도메인과 동일하게 query/write 유스케이스 분리
├── media        게시글 첨부(이미지/동영상/링크) — S3 presigned URL 업로드 + Rekognition 모더레이션. 2026-08-11 추가
└── common       횡단 관심사: 설정, 응답 포맷, 에러 처리, JWT 필터. 위 도메인별 구조에 억지로 끼워맞추지 않고 지금 형태를 유지.
    ├── config       SecurityConfig, JwtConfig/JwtProperties, RedisConfig, JpaConfig, OpenApiConfig, WebMvcConfig, SchedulingConfig(@EnableScheduling), AwsTranslateConfig/AwsS3Config/AwsRekognitionConfig
    ├── i18n         Language(10개 언어 enum), CountryLanguageResolver, LocaleResolver — 정적 UI 라벨 다국어(아래 참고)
    ├── security     JwtAuthenticationFilter (+ AuthPrincipal), security/jwt/JwtTokenProvider
    ├── web          MemberSno(애너테이션), MemberSnoArgumentResolver, LocaleController — 인증 컨트롤러 공통 파라미터 리졸버 + 국가→언어 조회
    ├── response     ResponseApi<T>
    └── error        ApiError, ErrorCode, GlobalExceptionHandler
```

`JwtTokenProvider`가 `member` 도메인이 아니라 `common/security/jwt/`에 있는 이유: `JwtAuthenticationFilter`(모든 도메인 요청이 거치는 필터)와 `SecurityConfig`가 이미 직접 의존하고 있어서, `member`의 adapter 밑에 두면 "공유 커널인 `common`이 특정 도메인의 어댑터에 의존"하는 역방향 의존이 생깁니다. JWT 서명/검증은 특정 도메인의 유스케이스가 아니라 순수 기술적 관심사이므로 `common`이 맞는 자리입니다. 반면 refresh token 발급/검증/폐기(`member/adapter/out/token/RedisRefreshTokenAdapter`, `RefreshTokenStorePort`)는 `member` 도메인의 유스케이스(로그인/재발급/로그아웃)를 위해서만 존재하므로 `member` 안에 남아있습니다.

### 도메인 간 의존 — 포트로만 넘나든다

이번 전환 전에는 `record.RecordServiceImpl`이 `rank`/`season`의 리포지토리를 직접 import해서 세 바운디드 컨텍스트가 한 클래스에 뒤섞여 있었고, `board.BoardQueryServiceImpl`도 닉네임 조회를 위해 `member`의 리포지토리를 직접 import했습니다. 지금은:

- `record` → `season.application.port.in.GetCurrentSeasonPort`(현재 시즌 조회), `rank.application.port.in.ApplyScoreUseCase`(점수 반영 — 예전 `RecordServiceImpl.saveRank`/`applyScoreToSeason` 로직이 통째로 `rank` 도메인 소유로 이전됨)에만 의존.
- `board` → `member.application.port.in.GetMemberNicknamePort`(작성자 닉네임), `rank.application.port.in.GetMemberTierPort`(작성자 티어뱃지), `record.application.port.in.RecordQueryService`(게시글 작성 시 첨부한 기록이 본인 소유인지 검증 — `getOne`은 원래 `record` 자신의 use-case지만 board가 그대로 재사용), `record.application.port.in.GetRecordSummaryPort`(게시글 조회 시 첨부 기록 요약 표시, 작성자가 아닌 제3자가 봐도 되도록 memberSno 소유권 검사 없이 recordSno만으로 조회)에 의존. 2026-08-11 "게시글에 러닝기록 첨부 + 작성자 프로필(티어)" 기능 추가 시 도입.
- `board` → `media.application.port.in.MediaService`(게시글 작성 시 첨부 확정 — `attachToPost`), `media.application.port.in.GetPostAttachmentsPort`(게시글 조회 시 첨부 목록/개수 조회, `record.GetRecordSummaryPort`와 동일하게 소유권 검사 없이 postSno로만 조회)에 의존. `media`는 반대로 `board`를 전혀 모른다(단방향 의존) — `attachToPost`가 `postSno`를 그냥 값으로 받아 저장할 뿐, board 도메인 타입을 참조하지 않음. 2026-08-11 "게시글에 이미지/동영상/링크 첨부" 기능 추가 시 도입.
- `rank`/`ranking` → `season.application.port.in.GetCurrentSeasonPort`에만 의존.

**예외 — 순수 정적 정책 클래스는 포트 없이 직접 import**: `board.application.service.BoardQueryServiceImpl`이 `rank.domain.policy.RankTierLabelPolicy`(티어 → 언어별 라벨 고정 테이블)를 포트 없이 바로 호출합니다. 위 "도메인 간 의존은 포트로만" 원칙의 예외인데, 이 클래스가 Spring 빈도 아니고 DB/Redis 접근도 없는 순수 정적 조회 함수(`RankTier`, `Language` 두 enum만 받아 `String`을 반환)라 포트/어댑터를 만드는 비용이 실익보다 크다고 판단했기 때문입니다 — `common` 패키지의 `StringRedisTemplate` 같은 범용 인프라 클라이언트를 포트화하지 않는 것과 같은 이유. 상태를 갖거나 DB에 접근하는 진짜 크로스 도메인 접근(리포지토리 등)이라면 반드시 포트를 통해야 합니다.

### 게시글 미디어 첨부(이미지/동영상/링크) — 2026-08-11 추가

`media` 도메인은 `record`/`rank`와 동일하게 query/write 유스케이스를 분리합니다(`MediaService`가 쓰기, `GetPostAttachmentsPort`를 구현하는 `MediaQueryServiceImpl`이 읽기). S3(`AwsS3Config`의 `S3Client`/`S3Presigner`)와 Rekognition(`AwsRekognitionConfig`의 `RekognitionClient`)은 `TranslateClient`와 동일하게 포트화하지 않고 `MediaServiceImpl`에 직접 주입합니다.

**record 첨부와 다른 지점 — `postSno` 연결 시점**: `record`는 게시글보다 먼저 존재하는 리소스를 참조만 하므로 `BoardServiceImpl.createPost`가 게시글을 저장하기 *전에* `recordQueryService.getOne(...)`으로 사전 검증합니다. 반면 미디어는 업로드 자체는 게시글 작성 이전에 끝나 있어도(파일 선택 즉시 S3 업로드+모더레이션이 진행됨) `media.post_sno`는 게시글이 실제로 저장돼 `post.sno`가 생긴 *이후에만* 채울 수 있습니다. 그래서 `BoardServiceImpl.createPost`는 `postRepositoryPort.save(post)` 다음에 `mediaService.attachToPost(memberSno, ids, post.getSno())`를 호출하고, 여기서 소유권/`APPROVED` 상태/중복첨부 검증에 실패해 예외가 던져지면 같은 `@Transactional` 메서드 안이므로 방금 저장한 게시글 insert까지 통째로 롤백됩니다 — "사전 검증 후 저장"이 아니라 "저장 후 검증 실패 시 롤백"으로 같은 원자성을 얻는 방식입니다.

**모더레이션 흐름**: `Media.status`는 `PENDING`(업로드 직후) → `APPROVED`/`REJECTED`로 전이합니다. `IMAGE`는 Rekognition `DetectModerationLabels`(동기 API)로 `/complete` 호출 안에서 바로 결과가 나오지만, `VIDEO`는 `StartContentModeration`(비동기 작업)만 시작하고 `PENDING`을 유지하다가, 클라이언트가 `GET /status`를 폴링할 때마다 `GetContentModeration`으로 작업 상태를 재조회해 갱신합니다(SNS/웹훅 없이 순수 폴링 — 인프라를 늘리지 않기 위한 선택). `REJECTED`가 확정되면(모더레이션 거부든 용량 초과든) S3 객체를 즉시 `DeleteObject`로 삭제합니다 — 버킷의 `media/` prefix가 전체 공개 읽기라, API가 URL을 응답에 노출하지 않아도 객체가 남아있으면 키를 아는 사람이 직접 접근할 수 있기 때문입니다(`docs/database.md#media` 참고). `LINK` 타입은 업로드/모더레이션 대상이 아니라 생성 즉시 `APPROVED`입니다.

**S3/Rekognition 인프라**: 버킷 `paceleague-media`(리전 `ap-northeast-2`, `media/` prefix만 공개 읽기, presigned `PUT`을 위한 버킷 CORS 별도 설정 — Spring `CorsConfig`와는 무관한 설정입니다). EC2 인스턴스 프로필 역할 `paceleague-s3-read`(`AwsTranslateConfig`가 이미 쓰던 역할)에 `s3:PutObject`/`GetObject`/`DeleteObject`(해당 prefix 한정)와 `rekognition:DetectModerationLabels`/`StartContentModeration`/`GetContentModeration`(Rekognition 모더레이션 액션은 리소스 레벨 ARN을 지원하지 않아 `"*"`) 권한이 필요합니다 — 2026-08-11 EC2 인스턴스 자체 권한으로 S3 PutObject/HeadObject/DeleteObject + Rekognition 호출을 직접 검증 완료(자세한 내용은 [infra.md](./infra.md) 참고).

### 게시글 본문 인라인 에디터 + 서버측 HTML sanitize — 2026-08-11 추가

위 미디어 첨부 기능이 나온 직후, 게시글 작성 UI 자체가 일반 `<textarea>`+분리된 업로드/링크 입력에서 **인라인 리치 에디터**(`contenteditable`, 굵게/기울임/링크/이미지/동영상만 지원)로 바뀌면서, `Post.content`가 평문에서 **공격자가 영향을 줄 수 있는 HTML**로 바뀌었습니다. 게시판은 비로그인도 읽을 수 있는 공개 콘텐츠이고 `web/js/app.js`가 JWT를 localStorage에 저장하므로, 저장형 XSS는 곧 토큰 탈취로 직결됩니다 — 그래서 **서버측 화이트리스트 sanitize가 필수**입니다(클라이언트 sanitize만으로는 절대 충분하지 않음).

`board.domain.policy.PostContentSanitizer`가 이 sanitize를 전담합니다. OWASP Java HTML Sanitizer(`com.googlecode.owasp-java-html-sanitizer`)를 쓰되, 번들 `Sanitizers.FORMATTING`/`Sanitizers.LINKS`/`Sanitizers.IMAGES`는 쓰지 않고 **커스텀 `HtmlPolicyBuilder`로 전체 화이트리스트를 직접 구성**합니다 — `Sanitizers.FORMATTING`은 `font,s,u,o,sup,sub,ins,del,strike,tt,code,big,small,span` 등 "굵게/기울임/링크만"보다 훨씬 넓은 태그를 허용해버리고, `video` 태그는 애초에 어떤 번들에도 없기 때문입니다. 최종 화이트리스트: `p, br, div, b, strong, i, em, a[href], img[src,alt], video[src,controls]` — 그 외(`script`, `style`, `on*` 속성, `class`, `javascript:` URL 등)는 라이브러리 기본 동작("명시 허용 외 전부 차단")으로 제거됩니다.

`BoardServiceImpl.createPost`가 클라이언트 입력을 받아 `Post.content`를 쓰는 유일한 경로이므로, 이 한 지점에서만 sanitize하면 됩니다(읽을 때마다 다시 sanitize하지 않음 — `web/post.html`이 `post.content`를 그대로 `innerHTML`에 꽂는 게 안전한 이유). "본문이 비어있는지" 판정도 여기서 재정의됩니다: sanitize 후 태그를 뺀 순수 텍스트가 비어 있어도 `<img>`/`<video>`가 하나라도 있으면 유효한 게시글로 인정합니다(이미지/동영상만 있는 글 허용) — `PostContentSanitizer.toPlainText`(정규식 기반 태그 제거, sanitize를 이미 거친 안전한 HTML에만 쓰므로 이 정도 수준으로 충분)와 `containsMedia`로 판정합니다.

`TranslationServiceImpl.translatePost`도 영향을 받습니다: AWS Translate는 HTML을 이해하지 못해 태그가 섞인 채로 넘기면 그대로 깨져서 번역되므로, `PostContentSanitizer.toPlainText(post.getContent())`로 평문만 추출해 번역합니다. 평문이 비어있으면(이미지 전용 글) Translate 호출 자체를 생략하고 빈 문자열을 반환합니다.

이미 구축된 `media` 도메인의 presign→PUT→complete→poll 파이프라인은 **전혀 변경되지 않았습니다** — 에디터는 그 결과(승인된 `url`)를 커서 위치의 placeholder에 삽입하는 방식으로만 바뀌었을 뿐입니다. `POST /api/media/links`도 API에는 남아 있지만, 웹 에디터는 더 이상 호출하지 않습니다(링크는 업로드/모더레이션이 필요 없어 `document.execCommand('createLink', ...)`로 즉시 삽입하고, sanitizer의 URL 프로토콜 화이트리스트가 유일한 검증). `PostSummaryResponse.attachmentCount`/`PostDetailResponse.attachments`(`GetPostAttachmentsPort` 기반)도 API에는 그대로 남아 있으나, 웹 클라이언트는 이미지/동영상이 `content` 안에 인라인으로 있으므로 더 이상 별도 갤러리를 그리지 않습니다(중복 표시 방지).

### 정적 UI 라벨 다국어(i18n) — 2026-08-11 추가

카테고리명(보드 name/description)과 티어 등급 표시는 웹 UI 언어 선택기(`web/js/i18n.js`, 10개 언어)에 맞춰 서버가 직접 번역된 문자열을 내려주도록 했습니다. 게시글 본문처럼 자유 텍스트가 아니라 **값의 종류가 고정**(보드 3개, 티어 7개)돼 있어 AWS Translate(`board.TranslationServiceImpl`, 유료·조회성 API)를 쓰지 않고, 코드 안에 고정 번역 테이블을 두는 방식을 택했습니다.

- `common.i18n.Language` — `web/js/i18n.js`의 `SUPPORTED_LANGUAGES`와 동일한 10개 코드(ko/en/ja/zh/es/fr/de/pt/vi/th)의 enum. `fromCode(String)`이 `null`/미지원 코드를 항상 `KO`로 폴백한다 (컨트롤러의 `translatePost` 같은 자유 번역 API는 미지원 언어를 400으로 거부하지만, 이건 정적 UI 라벨이라 관대하게 기본값으로 떨어지는 쪽을 택함).
- `rank.domain.policy.RankTierLabelPolicy` — `RankTier` 7개 × `Language` 10개 고정 테이블. `RankMeResponse`(`currentTierLabel`/`nextTierLabel`), `RankingUserResponse`(`tierLabel`), `PostSummaryResponse`/`PostDetailResponse`(`authorTierLabel`)에서 사용. 원본 `RankTier` enum 필드(`currentTier`/`tier`/`authorTier`)는 그대로 남겨둬서, 로직/필터링이 필요한 클라이언트(모바일 앱 등)는 언어와 무관한 코드를 계속 쓸 수 있다.
- `board.domain.policy.BoardLabelPolicy` — slug(`free`/`qna`/`verify`) × `Language` 9개(한국어 제외) 고정 테이블. 한국어는 DB의 `board.name`/`board.description`이 이미 원본이므로 테이블에 중복 저장하지 않고, `lang=ko`거나 테이블에 없는 slug(신규 보드 추가 시)면 항상 DB 값을 그대로 반환한다.
- 관련 GET 엔드포인트(`/api/board`, `/api/board/{boardSno}/posts`, `/api/board/posts/{postSno}`, `/api/rank/me`, `/api/ranking/getRanking`, `/api/ranking/top10`)가 전부 `lang` 쿼리 파라미터(기본값 `ko`)를 받는다 — 커스텀 헤더가 아닌 쿼리 파라미터라 기존 CORS 설정(`CorsConfig`) 변경이 필요 없었다.
- `common.i18n.CountryLanguageResolver` + `common.web.LocaleController`(`GET /api/common/language?country=KR`, 공개) — ISO 3166-1 alpha-2 국가코드를 위 10개 언어 코드 중 하나로 변환해, 위 `lang` 파라미터에 그대로 넣어 쓸 수 있게 한다. 매핑에 없는 국가나 값 미전달 시 `EN`으로 폴백하는데, 이는 `Language.fromCode`가 `KO`로 폴백하는 것과 의도적으로 다른 선택 — "브라우저가 이미 보내는 언어 문자열을 못 알아들었다"와 "국가 자체를 매핑 테이블에 안 넣어놨다"는 서로 다른 상황이라 각각 자연스러운 기본값(전자는 이 서비스의 원래 언어인 한국어, 후자는 국제 공용어인 영어)을 골랐다. DB 접근이 없는 순수 정적 조회라 도메인/포트/서비스 계층 없이 컨트롤러가 리졸버를 바로 호출한다. `web/js/i18n.js`는 이미 브라우저에서 자체적으로 언어를 판별하므로 이 엔드포인트를 쓰지 않음 — 국가 기반 판별이 필요한 모바일 앱 등 다른 클라이언트를 위한 API.
- `common.i18n.LocaleResolver` — 위 `/api/common/language`만으로는 "언어 코드를 알아내는 것"과 "그 언어로 번역된 실제 데이터를 받는 것" 사이에 호출이 한 번 더 필요해서(클라이언트가 언어를 알아낸 뒤 그 코드로 다시 요청해야 함), `lang`을 받는 모든 엔드포인트(`/api/board`, `/api/board/{boardSno}/posts`, `/api/board/posts/{postSno}`, `/api/rank/me`, `/api/ranking/getRanking`, `/api/ranking/top10`)가 `country` 파라미터도 함께 받도록 했다. `LocaleResolver.resolve(lang, country)`가 `country`가 있으면 그걸 우선시하고(`CountryLanguageResolver`로 변환), 없으면 `lang`을 그대로 쓴다 — 컨트롤러 레벨에서만 해석하고 `Language.toCode()`로 문자열로 바꿔 기존 서비스 시그니처(`String lang`)에 그대로 흘려보내므로 서비스/포트 계층은 변경되지 않았다.

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
