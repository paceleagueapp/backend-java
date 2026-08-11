# API 명세

## 공통 사항

### Base URL

로컬: `http://localhost:8080`

### 공통 응답 포맷

모든 컨트롤러 응답은 `ResponseApi<T>`로 래핑됩니다 (`common.response.ResponseApi`).

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "정상 처리되었습니다.",
  "data": { },
  "timestamp": "2026-07-12T03:00:00Z"
}
```

- `success`: 처리 성공 여부
- `code`: `"SUCCESS"` 또는 에러 코드 문자열
- `message`: 사람이 읽을 수 있는 메시지 (엔드포인트별로 다름)
- `data`: 실제 응답 페이로드 (없으면 `null`)
- `timestamp`: 서버 응답 시각 (ISO-8601 Instant)

### 에러 응답 포맷

`GlobalExceptionHandler`가 처리하는 예외는 `ApiError`로 응답합니다 (`ResponseApi`가 아님에 주의).

```json
{
  "code": "BAD_REQUEST",
  "message": "이미 존재하는 아이디입니다.",
  "timestamp": "2026-07-12T03:00:00Z"
}
```

| 예외 | HTTP 상태 | `code` |
|---|---|---|
| `IllegalArgumentException` (서비스 계층의 검증 실패) | 400 | `BAD_REQUEST` |
| 그 외 미처리 예외 | 500 | `INTERNAL_ERROR` (message는 항상 `"서버 오류"`로 고정, 상세 내용은 서버 로그에만 기록) |

인증/인가 실패는 `GlobalExceptionHandler`가 아니라 `SecurityConfig`의 `exceptionHandling`에서 직접 JSON을 작성합니다 (아래 두 응답은 `ApiError`/`ResponseApi` 스키마와 다른 별도 포맷):

```json
// 401 Unauthorized (인증 안 됨 / 토큰 없음·무효)
{ "success": false, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }

// 403 Forbidden (인가 실패)
{ "success": false, "code": "FORBIDDEN", "message": "접근 권한이 없습니다." }
```

### 인증

- 인증이 필요한 API는 `Authorization: Bearer <accessToken>` 헤더가 필요합니다.
- 토큰이 없거나 무효/만료된 상태로 인증 필요 API를 호출하면 401이 반환됩니다 (`JwtAuthenticationFilter`는 예외를 던지지 않고 `SecurityContext`를 비운 채 통과시키며, 이후 `anyRequest().authenticated()` 규칙에서 401로 이어짐).
- 인증 없이 호출 가능한(공개) 엔드포인트는 아래 표에 "공개"로 표시됩니다. 나머지는 전부 인증 필요.
- 인증된 요청에서 컨트롤러는 토큰의 `sub` 클레임(memberSno)을 `Authentication.getPrincipal()`을 `JwtAuthenticationFilter.AuthPrincipal`로 캐스팅해 꺼내 씁니다. 이 패턴은 인증된 모든 컨트롤러에 반복되며 별도 리졸버/애노테이션으로 추상화되어 있지 않습니다.
- Swagger UI: `/swagger-ui.html`, `/swagger-ui/**`, OpenAPI 스펙: `/v3/api-docs/**` (둘 다 공개).

### 공개(인증 불필요) 엔드포인트 목록

- `POST /api/member/join`
- `POST /api/member/login`
- `POST /api/member/reissue`
- `POST /api/member/logout`
- `GET /api/app/version-check`
- `GET /api/ranking/top10`
- `GET /robots.txt` (검색엔진 크롤링 전면 차단용, [infra.md](./infra.md#검색엔진-크롤링-차단-apipaceleaguecokr) 참고)
- `/v3/api-docs/**`, `/swagger-ui.html`, `/swagger-ui/**`
- `GET /api/board`, `GET /api/board/{boardSno}/posts`, `GET /api/board/posts/{postSno}`, `GET /api/board/posts/{postSno}/comments` (게시판 조회 4종만 공개 — 작성/삭제/추천은 인증 필요, [Board API](#board-api-apiboard--조회는-공개-작성삭제추천은-인증-필요) 참고)

그 외 모든 엔드포인트는 인증이 필요합니다.

---

## Member API (`/api/member`)

인증 도메인. 회원가입/로그인/토큰 재발급/로그아웃.

### POST `/api/member/join` — 회원가입 (공개)

가입 후 access/refresh token을 즉시 발급합니다.

**Request Body** (`JoinRequest`)

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| memberId | string | Y | not blank, max 50자 |
| password | string | Y | not blank, 4~100자 |
| nickname | string | N | max 50자 |
| email | string | N | max 50자 |

```json
{
  "memberId": "runner01",
  "password": "pass1234",
  "nickname": "러너01",
  "email": "runner01@example.com"
}
```

**Response** `200 OK` — `data`: `TokenResponse` (아래 "Token 응답 공통 포맷" 참고)

**실패**
- `memberId`가 이미 존재 → 400 `"이미 존재하는 아이디입니다."`
- validation 실패(예: password 4자 미만) → 400 (Spring `@Valid` 바인딩 에러)

### POST `/api/member/login` — 로그인 (공개)

**Request Body** (`LoginRequest`)

| 필드 | 타입 | 필수 |
|---|---|---|
| memberId | string | Y |
| password | string | Y |

**Response** `200 OK` — `data`: `TokenResponse`

**실패**: 아이디 없음 또는 비밀번호 불일치 → 400 `"아이디 또는 비밀번호가 올바르지 않습니다."` (아이디/비밀번호 실패 원인을 구분해 노출하지 않음)

### POST `/api/member/reissue` — 토큰 재발급 (공개)

refresh token으로 새 access/refresh token 쌍을 발급합니다. **refresh token은 재발급 시 즉시 회수(rotation)** 되며, 재사용할 수 없습니다.

**Request Body** (`TokenReissueRequest`)

```json
{ "refreshToken": "5f3e2a..." }
```

**Response** `200 OK` — `data`: `TokenResponse` (새 refresh token 포함)

**실패**
- `refreshToken`이 비어있음 → 400 `"refresh token is required"`
- Redis에 없음(만료/이미 사용됨/위조) → 400 `"refresh token expired or invalid"`

### POST `/api/member/logout` — 로그아웃 (공개)

전달된 refresh token을 Redis에서 즉시 삭제(폐기)합니다. **access token 자체는 무효화되지 않고** 남은 TTL 동안 유효합니다 (블랙리스트 미구현).

**Request Body** (`LogoutRequest`)

```json
{ "refreshToken": "5f3e2a..." }
```

**Response** `200 OK` — `data`: `"로그아웃이 완료되었습니다."` (string)

**실패**: `refreshToken`이 비어있음 → 400 `"refresh token is required"`

### Token 응답 공통 포맷 (`TokenResponse`)

join/login/reissue가 공통으로 반환하는 구조:

```json
{
  "tokenType": "Bearer",
  "accessToken": "eyJhbGciOi...",
  "accessExpiresInSeconds": 300,
  "refreshToken": "5f3e2a9c...",
  "refreshExpiresInSeconds": 1209600,
  "nickname": "러너01"
}
```

기본값(운영/로컬 공통 설정): access token 300초(5분), refresh token 1,209,600초(14일). 자세한 토큰 구조는 [auth.md](./auth.md) 참고.

---

## Record API (`/api/record`) — 인증 필요

러닝 기록 저장/조회. 거리 단위는 **미터**(`distanceRecord`)로 저장하며, 응답의 요약(pace/calorie) 계산 시 내부적으로 km로 환산합니다.

### POST `/api/record/save` — 기록 단건 저장

**Request Body** (`RecordCreateRequest`)

| 필드 | 타입 | 설명 |
|---|---|---|
| distanceRecord | number (BigDecimal) | 달린 거리, **미터** 단위 |
| startTime | string (`yyyy-MM-ddTHH:mm:ss`) | 시작 시각 (LocalDateTime, 타임존 정보 없음) |
| endTime | string (`yyyy-MM-ddTHH:mm:ss`) | 종료 시각 |
| utcOffset | string | 클라이언트의 UTC 오프셋 (예: `"+09:00"`), 서버는 그대로 저장만 함 |

```json
{
  "distanceRecord": 5230.5,
  "startTime": "2026-07-10T06:30:00",
  "endTime": "2026-07-10T07:05:00",
  "utcOffset": "+09:00"
}
```

서버 측 검증(컨트롤러가 아닌 서비스 계층에서 수동 검증, `@Valid` 미사용):
- `distanceRecord`, `startTime`, `endTime` 모두 필수
- `endTime`은 `startTime`보다 늦어야 함
- **중복 방지**: 최근 1개월 내 동일한 `startTime`을 가진 기록이 이미 있으면 저장 거부 (`"duplicate record"`)

저장과 동시에 해당 기록에 대한 점수를 계산해 `Rank`(점수 로그) 및 시즌별 `MemberScore`(누적 점수)에 반영합니다. 점수 산정 규칙은 [domains.md](./domains.md#기록-저장-시-점수-산정-로직) 참고.

**Response** `200 OK` — `data`: `{ "sno": 123 }`

**실패**
- `uno` 유효하지 않음(인증 실패로는 발생하지 않으나 방어 코드로 존재) → 400
- 필수값 누락/시간 역전 → 400
- 중복 기록 → 400 `"duplicate record"`

### POST `/api/record/bulk` — 기록 일괄 저장

**Request Body**: `RecordCreateRequest[]` (배열, 최대 **200건**)

동작 방식이 단건 저장과 다릅니다:
- 배열 전체를 먼저 검증(각 항목에 대해 단건과 동일한 검증)
- 최근 1개월 내 기존 `startTime` 집합과 겹치는 항목은 **에러 없이 조용히 스킵**됨 (같은 요청 내 중복 `startTime`끼리도 마찬가지)
- 스킵되지 않은 항목만 저장되고, 각 저장 건마다 점수 계산이 수행됨

**Response** `200 OK` — `data`: `{ "savedSnos": [123, 124, 125] }` (실제로 저장된 건만 포함, 스킵된 건은 빠짐)

**실패**
- 배열이 비어있음 → 400 `"records is empty"`
- 200건 초과 → 400 `"too many records (max 200)"`

### GET `/api/record/dataOne/{sno}` — 기록 단건 조회

본인 소유 기록만 조회 가능(`uno` + `sno` 조합으로 조회).

**Response** `200 OK` — `data`: `RecordResponse`

```json
{
  "sno": 123,
  "uno": 1,
  "distanceRecord": 5230.5,
  "startTime": "2026-07-10T06:30:00",
  "endTime": "2026-07-10T07:05:00",
  "createAt": "2026-07-10T07:05:10",
  "updateAt": "2026-07-10T07:05:10"
}
```

**실패**: 존재하지 않거나 타인의 기록 → 400 `"record not found"`

### GET `/api/record/dataPage` — 기록 페이징 조회

**Query Parameters**

| 파라미터 | 타입 | 기본값 |
|---|---|---|
| page | int | 0 |
| size | int | 10 (0 이하로 주면 10으로 보정) |

정렬: `startTime` 내림차순(최신순) 고정.

**Response** `200 OK` — `data`: Spring `Page<RecordResponse>` (표준 Spring Data 페이지 JSON: `content`, `totalElements`, `totalPages`, `number`, `size` 등)

### GET `/api/record/dataMonth` — 월간 기록 전체 조회

**Query Parameters**

| 파라미터 | 타입 | 필수 |
|---|---|---|
| year | int | Y |
| month | int (1~12) | Y |

**Response** `200 OK` — `data`: `RecordMonthResponse`

```json
{
  "memberSummary": {
    "totalDistance": 152300.0000,
    "totalDurationSeconds": 54000,
    "paceSecondsPerKm": 354,
    "paceText": "5:54 /km",
    "totalCalories": 10661.0
  },
  "monthSummary": { "...": "memberSummary와 동일한 구조, 해당 연/월로 필터링" },
  "records": [ /* RecordResponse DTO 배열 */ ]
}
```

- `memberSummary`: 가입 이후 **전체 기간** 누적 요약
- `monthSummary`: 요청한 `year`/`month`의 요약
- `records`: 해당 월의 전체 기록 목록, `RecordResponse` DTO로 변환되어 반환 (시작 시각 오름차순)
- **알려진 이슈**: 칼로리 계산에 쓰이는 체중이 실제 회원 체중이 아니라 `70kg`로 하드코딩되어 있음 (`RecordController`에 TODO 명시).

**실패**: `month`가 1~12 범위를 벗어남 → 400 `"month must be 1~12"`

### GET `/api/record/recent-30-days` — 최근 30일 기록 조회

쿼리 파라미터 없음. 오늘 기준 최근 30일 내 기록을 최신순으로 반환.

**Response** `200 OK` — `data`: `RunningRecordResponse[]`

```json
[
  {
    "recordSno": 123,
    "startTime": "2026-07-10T06:30:00",
    "endTime": "2026-07-10T07:05:00",
    "distance": 5230.5,
    "createAt": "2026-07-10T07:05:10"
  }
]
```

> 이 엔드포인트는 다른 GET 엔드포인트와 달리 `ResponseEntity`로 감싸지 않고 `ResponseApi<T>`를 직접 반환합니다 (동작상 차이는 없음, 응답 포맷은 동일).

> **CORS**: 게시글 작성 화면에서 "내 러닝기록 첨부" 선택 목록으로 쓰기 위해, `/api/record`의 다른 엔드포인트와 달리 이 경로만 예외적으로 `paceleague.co.kr`/`www.paceleague.co.kr` 오리진에서 브라우저 호출을 허용합니다(`CorsConfig`, GET + `Authorization` 헤더만).

---

## Rank API (`/api/rank`) — 인증 필요

"내 점수/티어"를 조회하는 개인 관점 API. 리더보드는 [Ranking API](#ranking-api-apiranking--인증-필요) 참고.

### GET `/api/rank/me` — 내 랭크 조회

현재 시즌 기준 내 누적 점수, 현재 티어, 다음 티어까지 남은 점수를 반환합니다.

**Response** `200 OK` — `data`: `RankMeResponse`

```json
{
  "totalScore": 1620,
  "currentTier": "SILVER",
  "nextTier": "GOLD",
  "nextTierRequiredScore": 3000,
  "remainingScore": 1380
}
```

- 이번 시즌에 아직 기록이 없는 회원은 기본값(`totalScore = 1500`, `currentTier = SILVER`)으로 응답합니다.
- `nextTier`/`nextTierRequiredScore`/`remainingScore`는 최고 티어(`CHALLENGER`)에서는 각각 `null`/`0`/`0`.
- 티어 구간은 [domains.md](./domains.md#티어-rank-tier) 참고.

---

## Ranking API (`/api/ranking`)

전체 리더보드(Top3) + 내 주변 순위를 함께 반환하는 API. `top10`만 예외적으로 공개(인증 불필요) 엔드포인트입니다.

### GET `/api/ranking/getRanking` — 랭킹 페이지 조회 (인증 필요)

**Response** `200 OK` — `data`: `RankingPageResponse`

```json
{
  "topRanks": [
    { "rank": 1, "memberSno": 7, "nickname": "페이서", "totalScore": 24500, "tier": "CHALLENGER", "me": false },
    { "rank": 2, "memberSno": 3, "nickname": "런닝맨", "totalScore": 19800, "tier": "MASTER", "me": false },
    { "rank": 3, "memberSno": 1, "nickname": "러너01", "totalScore": 15200, "tier": "MASTER", "me": true }
  ],
  "aroundRanks": [
    { "rank": 1, "memberSno": 1, "nickname": "러너01", "totalScore": 15200, "tier": "MASTER", "me": true },
    { "rank": 2, "memberSno": 9, "nickname": "..." , "totalScore": 14990, "tier": "MASTER", "me": false }
  ]
}
```

- `topRanks`: 시즌 전체 1~3위 (항상 최대 3명)
- `aroundRanks`: 내 순위를 중심으로 앞뒤 포함 **5명** (내 순위가 3위 이하로 밀려있어도 상위 유저부터 잘리지 않도록 `offset = max(0, 내 순위 - 3)`으로 계산)
- 두 목록 모두 각 항목에 `me: true/false`로 본인 여부 표시
- 순위 산정 로직(동점자 처리 포함)은 [domains.md](./domains.md#랭킹리더보드-산정-로직) 참고

### GET `/api/ranking/top10` — 상위 10명 랭킹 조회 (공개, 인증 불필요)

`paceleague.co.kr` 랜딩 페이지(`web/index.html`)에서 표시하기 위한 공개 엔드포인트입니다. 시즌 상위 10명을 그대로 반환합니다 (내부적으로 `getRanking`의 "내 주변 순위" 조회와 같은 쿼리를 `offset=0, limit=10`으로 재사용).

**Response** `200 OK` — `data`: `RankingUserResponse[]`

```json
[
  { "rank": 1, "memberSno": 7, "nickname": "페이서", "totalScore": 24500, "tier": "CHALLENGER", "me": false },
  { "rank": 2, "memberSno": 3, "nickname": "런닝맨", "totalScore": 19800, "tier": "MASTER", "me": false }
]
```

- 로그인 컨텍스트가 없으므로 `me`는 항상 `false`.
- CORS: `https://paceleague.co.kr`, `https://www.paceleague.co.kr` 오리진에서만 브라우저 `fetch`로 호출 가능하도록 이 경로에만 한정해 CORS를 허용합니다 (`common.config.CorsConfig`). 다른 API는 CORS를 열지 않았으므로 브라우저에서 다른 오리진으로는 호출할 수 없습니다.

---

## AppVersion API (`/api/app`) — 공개

모바일 앱의 강제/선택 업데이트 및 점검 여부를 체크하는 API. 앱 부팅 시 호출하는 용도이므로 인증 불필요.

### GET `/api/app/version-check` — 버전 체크

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| platform | string (`ANDROID` \| `IOS`) | Y | |
| appVersion | string (`x.y.z` 형식) | Y | 클라이언트의 현재 앱 버전 |

**Response** `200 OK` — `data`: `AppVersionCheckResponse`

```json
{
  "platform": "ANDROID",
  "currentVersion": "1.2.0",
  "latestVersion": "1.4.0",
  "minRequiredVersion": "1.3.0",
  "updateType": "FORCE",
  "forceUpdate": true,
  "storeUrl": "https://play.google.com/store/apps/details?id=...",
  "message": "새로운 버전이 있습니다.",
  "maintenance": false,
  "maintenanceMessage": null
}
```

`updateType` 판정 규칙(버전은 `.`으로 split 후 세그먼트별 숫자 비교):
- `currentVersion < minRequiredVersion` → `FORCE` (`forceUpdate: true`)
- `minRequiredVersion <= currentVersion < latestVersion` → `OPTIONAL`
- 그 외 → `NONE`

**실패**: 해당 `platform`에 대한 정책이 DB에 없음 → 400 `"앱 버전 정책이 존재하지 않습니다. platform=..."`

## Board API (`/api/board`) — 조회는 공개, 작성/삭제/추천은 인증 필요

커뮤니티(보드/게시글/댓글/추천). 레딧처럼 **조회(GET)는 비로그인도 가능**하고, 글/댓글 작성·삭제·추천처럼 쓰기 작업(POST/DELETE)만 로그인이 필요합니다. 웹(`paceleague.co.kr`/`www.paceleague.co.kr`)에서 브라우저로 직접 호출하므로 CORS가 열려 있습니다(`CorsConfig`의 `/api/board/**` 등록, [architecture.md](./architecture.md) 참고).

비로그인으로 조회하면 `myVote`는 항상 `null`입니다(내 투표 여부를 알 수 없으므로).

댓글은 **1단계 중첩만 허용**됩니다 — 최상위 댓글에는 답글을 달 수 있지만, 답글에는 답글을 달 수 없습니다(`parentCommentSno`가 이미 있는 댓글을 다시 `parentCommentSno`로 지정하면 400).

추천/비추천은 토글 방식입니다: 같은 값으로 다시 요청하면 추천 취소, 다른 값으로 요청하면 전환, 처음이면 신규 생성.

### GET `/api/board` — 보드 목록 조회 (공개)

**Response** `200 OK` — `data`: `BoardResponse[]`

```json
[
  { "sno": 1, "slug": "free", "name": "자유게시판", "description": "자유롭게 이야기하는 공간입니다." }
]
```

### GET `/api/board/{boardSno}/posts` — 게시글 목록 조회 (공개)

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| page | int | 0 | |
| size | int | 20 | |
| sort | string (`new`\|`top`) | `new` | `top`은 `score DESC, create_at DESC` |

**Response** `200 OK` — `data`: Spring `Page<PostSummaryResponse>` (`sno`, `title`, `nickname`, `authorTier`, `recordSno`, `viewCount`, `score`, `commentCount`, `createAt`)

- `authorTier`: 작성자의 현재 시즌 티어(`RankTier` enum, `rank.GetMemberTierPort`로 조회, 이번 시즌 기록이 없으면 기본값 `SILVER`)
- `recordSno`: 작성 시 첨부한 기록의 PK(없으면 `null`). 목록에서는 첨부 여부 표시용으로만 쓰고, 기록 상세 내용은 게시글 상세 조회에서만 내려줌

**실패**: 존재하지 않는 `boardSno` → 400

### POST `/api/board/{boardSno}/posts` — 게시글 작성 (인증 필요)

```json
{ "title": "오늘 10km 완주!", "content": "날씨가 좋아서 기분 좋게 뛰었습니다.", "recordSno": 123 }
```

`recordSno`는 선택 항목입니다. 지정하면 본인 소유 기록인지 검증합니다(`GET /api/record/recent-30-days` 등으로 조회한 본인 기록의 `recordSno`만 사용 가능 — 기간 제한은 없고, 다른 회원 소유이거나 존재하지 않는 `recordSno`면 거부됩니다).

**Response** `200 OK` — `data`: `{ "sno": 123 }`

**실패**: `title`/`content` 공백 또는 `title` 200자 초과 → 400, 존재하지 않는 `boardSno` → 400, `recordSno`가 존재하지 않거나 본인 소유가 아님 → 400 `"record not found"`

### GET `/api/board/posts/{postSno}` — 게시글 상세 조회 (공개)

조회할 때마다 `view_count`가 1 증가합니다(중복 방지 없음). `myVote`는 내가 이 글에 투표한 값(`1`/`-1`/`null`), 비로그인이면 항상 `null`.

**Response** `200 OK` — `data`: `PostDetailResponse`

```json
{
  "sno": 123, "boardSno": 1, "boardName": "자유게시판",
  "title": "오늘 10km 완주!", "content": "...",
  "memberSno": 5, "nickname": "러너1", "authorTier": "GOLD",
  "attachedRecord": {
    "recordSno": 456, "startTime": "2026-08-08T06:30:00", "endTime": "2026-08-08T07:05:00",
    "distance": 10230.5, "createAt": "2026-08-08T07:05:10"
  },
  "viewCount": 12, "score": 3, "myVote": 1,
  "createAt": "2026-08-08T10:00:00", "updateAt": "2026-08-08T10:00:00"
}
```

- `authorTier`: 목록 조회와 동일하게 작성자의 현재 시즌 티어
- `attachedRecord`: 첨부한 기록이 없으면 `null`. 첨부된 기록이 이후 삭제된 경우에도 `null`로 응답(참조 무결성을 강제하지 않음)

**실패**: 존재하지 않는 `postSno` → 400

### DELETE `/api/board/posts/{postSno}` — 게시글 삭제 (인증 필요)

본인 게시글만 삭제 가능. 그 게시글의 댓글/대댓글/추천 기록도 함께 삭제됩니다(하드 삭제, 복구 불가).

**실패**: 존재하지 않거나 본인 소유가 아님 → 400

### POST `/api/board/posts/{postSno}/vote` — 게시글 추천/비추천 (인증 필요)

```json
{ "voteValue": 1 }
```

`voteValue`는 `1`(추천) 또는 `-1`(비추천). **Response** `200 OK` — `data`: `{ "score": 4, "myVote": 1 }` (취소된 경우 `myVote: null`)

**실패**: `voteValue`가 1/-1이 아님 → 400, 존재하지 않는 `postSno` → 400

### GET `/api/board/posts/{postSno}/comments` — 댓글 목록 조회 (공개)

최상위 댓글과 그 답글(1단계)만 포함, 페이징 없음.

**Response** `200 OK` — `data`: `CommentResponse[]`

```json
[
  {
    "sno": 10, "memberSno": 5, "nickname": "러너1", "content": "축하해요!", "score": 2, "myVote": null,
    "createAt": "2026-08-08T10:05:00",
    "replies": [
      { "sno": 11, "memberSno": 6, "nickname": "러너2", "content": "감사합니다", "score": 0, "myVote": null, "createAt": "2026-08-08T10:06:00", "replies": [] }
    ]
  }
]
```

### POST `/api/board/posts/{postSno}/comments` — 댓글/답글 작성 (인증 필요)

```json
{ "content": "축하해요!", "parentCommentSno": null }
```

`parentCommentSno`를 지정하면 그 댓글의 답글로 작성됩니다. **Response** `200 OK` — `data`: `{ "sno": 10 }`

**실패**: `content` 공백 또는 1000자 초과 → 400, 존재하지 않는 `postSno`/`parentCommentSno` → 400, `parentCommentSno`가 다른 게시글의 댓글이거나 이미 답글임(답글에 답글 시도) → 400

### DELETE `/api/board/comments/{commentSno}` — 댓글 삭제 (인증 필요)

본인 댓글만 삭제 가능. 그 댓글의 답글/추천 기록도 함께 삭제됩니다.

**실패**: 존재하지 않거나 본인 소유가 아님 → 400

### POST `/api/board/comments/{commentSno}/vote` — 댓글 추천/비추천 (인증 필요)

게시글 추천과 동일한 토글 규칙. `voteValue`: `1`\|`-1`. **Response** `200 OK` — `data`: `VoteResponse`

### POST `/api/board/posts/{postSno}/translate` — 게시글 번역 (인증 필요)

제목/본문을 대상 언어로 번역합니다. **조회성 동작이지만 예외적으로 로그인이 필요합니다** — AWS Translate 호출 비용이 드는 유일한 board 엔드포인트라, 비로그인 남용으로 비용이 새는 걸 막기 위함([domains.md](./domains.md) 참고). 결과는 Redis에 `(postSno, targetLanguage)` 기준 180일 캐싱되어 같은 조합 재요청 시 API를 다시 호출하지 않습니다.

```json
{ "targetLanguage": "en" }
```

지원 언어: `ko`, `en`, `ja`, `zh`, `es`, `fr`, `de`, `pt`, `vi`, `th`. **Response** `200 OK` — `data`: `{ "title": "...", "content": "..." }`

**실패**: `targetLanguage`가 지원 목록 밖 → 400, 존재하지 않는 `postSno` → 400

### POST `/api/board/comments/{commentSno}/translate` — 댓글 번역 (인증 필요)

게시글 번역과 동일한 방식(캐싱/지원 언어/인증 이유 동일). `data`: `{ "content": "..." }`
