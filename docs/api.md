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
- `/v3/api-docs/**`, `/swagger-ui.html`, `/swagger-ui/**`

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
  "records": [ /* Record 엔티티 배열 (JPA 엔티티가 그대로 직렬화됨) */ ]
}
```

- `memberSummary`: 가입 이후 **전체 기간** 누적 요약
- `monthSummary`: 요청한 `year`/`month`의 요약
- `records`: 해당 월의 전체 기록 목록 (시작 시각 오름차순)
- **알려진 이슈**: 칼로리 계산에 쓰이는 체중이 실제 회원 체중이 아니라 `70kg`로 하드코딩되어 있음 (`RecordController`에 TODO 명시).
- **알려진 이슈**: `records`는 `RecordResponse` DTO가 아닌 `Record` JPA 엔티티가 그대로 직렬화됨.

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

## Ranking API (`/api/ranking`) — 인증 필요

전체 리더보드(Top3) + 내 주변 순위를 함께 반환하는 API.

### GET `/api/ranking/getRanking` — 랭킹 페이지 조회

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
