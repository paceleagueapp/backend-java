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
- Swagger UI: `/swagger-ui.html`, `/swagger-ui/**`, OpenAPI 스펙: `/v3/api-docs/**`. **로컬 개발 환경 전용** — 운영(`prod` 프로파일)에서는 `springdoc.api-docs.enabled=false` / `springdoc.swagger-ui.enabled=false`로 꺼져 있어 두 경로 모두 404입니다. `SecurityConfig`의 permitAll 목록에는 남아 있지만 운영에서는 핸들러 자체가 등록되지 않습니다.

### 공개(인증 불필요) 엔드포인트 목록

- `POST /api/member/join`
- `POST /api/member/login`
- `POST /api/member/reissue`
- `POST /api/member/logout`
- `GET /api/app/version-check`
- `GET /api/ranking/top10`
- `GET /api/common/language`
- `GET /robots.txt` (검색엔진 크롤링 전면 차단용, [infra.md](./infra.md#검색엔진-크롤링-차단-apipaceleaguecokr) 참고)
- `/v3/api-docs/**`, `/swagger-ui.html`, `/swagger-ui/**` (로컬 전용 — 운영에서는 springdoc이 비활성화되어 404)
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

### GET `/api/member/search` — 회원 검색 (인증 필요)

크루 초대 대상 등을 아이디/닉네임으로 찾습니다. `member_id` 접두 일치 또는 `nickname` 부분 일치(아이디 정확 일치 우선), 최대 20건.

| Query | 설명 |
|---|---|
| q | 검색어(아이디/닉네임). **필수** |

**Response** `200 OK` — `data`: `MemberSearchResult[]`

```json
[ { "memberSno": 42, "memberId": "runner01", "nickname": "달리는곰" } ]
```

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

### POST `/api/record/gps` — GPS 청크 수신 (러닝 중 5분마다)

앱이 러닝 중 **5분마다** 그 사이 수집한 GPS 좌표 청크를 보냅니다. 서버는 `clientRunId`로 같은 러닝을 묶어 `record_track` 한 행에 누적하고, 거리는 좌표에서 직접 계산(haversine)합니다. **마지막 청크에 `finished: true`** 가 실리면 그때 **딱 한 번** 러닝 기록(`record`) 1건을 생성하고 점수를 산정합니다. 회원 식별은 access token(`Authorization: Bearer ...`)으로만 합니다. 모바일 앱 전용이라 CORS는 열지 않습니다.

**Request Body** (`GpsSessionRequest`)

| 필드 | 타입 | 설명 |
|---|---|---|
| clientRunId | string | 앱이 러닝 시작 시 만든 고유 ID. 그 러닝의 모든 청크가 같은 값을 보냄. 최대 100자. **필수** |
| points | array | 이번 5분 청크의 좌표 배열. 각 원소: `{ sequence, recordedAt, latitude, longitude, altitudeMeters, accuracyMeters, rawLatitude, rawLongitude }` — `recordedAt`/`latitude`/`longitude`만 필수. 한 청크 최대 2,000개. `finished: true` 이면서 더 보낼 좌표가 없으면 `[]` 또는 생략 가능 |
| finished | boolean | 마지막 청크에서 `true`. 기본 `false` |
| activityType | string | 선택. 생략 시 `"RUNNING"` 간주. `"RUNNING"` 외 값은 거부 |
| location | object | 선택(보통 첫 청크만). `{ requestedIntervalMs, distanceFilterMeters, algorithmVersion }` |
| device | object | 선택(보통 첫 청크만). `{ platform, appVersion, appBuildNumber }` |
| schemaVersion | int | 선택 |
| utcOffset | string | 선택. 앱이 넣어주면 그대로 `record_track.utc_offset` → 종료 시 `record.utc_offset`에 저장(예: `"+09:00"`) |
| territoryMode | boolean | 선택. 러닝 시작 시 "땅따먹기 모드"로 시작했으면 `true`. 첫 청크에만 실으면 되고 세션 생성 시점에 확정(이후 불변). `true`인 세션만 러닝 종료 시 땅따먹기 판정(땅 생성/데미지/점령)이 돈다 — [Territory API](#territory-api-apiterritory--러닝-땅따먹기) 참고. 없으면 `false`(일반 러닝) |

시각(`recordedAt`)은 ISO-8601 UTC(예: `"2026-08-26T10:46:25.797Z"`)로 보내며, 서버가 UTC `LocalDateTime`으로 변환해 저장합니다.

**Response** `200 OK` — `data`: `GpsSessionResponse`

```json
{ "clientRunId": "1787741158726094-2887287643", "status": "ACTIVE",
  "chunkSeq": 3, "acceptedPoints": 98, "skippedPoints": 2,
  "totalPoints": 412, "distanceMeters": 3120.54, "recordSno": null,
  "territoryResult": null }
```

| 필드 | 설명 |
|---|---|
| status | `ACTIVE`(진행 중) 또는 `FINISHED`(종료·`record` 생성됨) |
| chunkSeq | 지금까지 누적된 청크 수 |
| acceptedPoints | 이번 요청에서 새로 저장된 좌표 수 |
| skippedPoints | 이미 저장된 마지막 좌표 시각보다 이전이라 무시된 좌표 수(청크 재전송 대비) |
| totalPoints / distanceMeters | 러닝 전체 누적 좌표 수 / 거리(m) |
| recordSno | 종료 전에는 `null`, `finished: true` 처리 후 생성된 `record.sno` |
| territoryResult | **땅따먹기 모드(`territoryMode: true`) 세션이 이번 요청으로 종료됐을 때만** 채워짐. 그 외(진행 중 / 일반 러닝 / 이미 종료된 세션 재전송)에는 `null`. 앱이 러닝 종료 화면에서 "새 땅 점령!" 같은 피드백을 표시하는 용도 (아래) |

**`territoryResult`** (`finished: true` + `territoryMode` 세션에서만)

```json
{ "outcome": "INTERACTED",
  "createdTerritorySno": null,
  "capturedTerritories": [ { "territorySno": 7, "previousOwnerMemberSno": 42, "previousOwnerNickname": "느린거북" } ],
  "damagedTerritorySnos": [ 12, 15 ],
  "healedTerritorySnos": [] }
```

| 필드 | 설명 |
|---|---|
| outcome | `NO_LOOP`(경로가 닫힌 도형이 아님) / `INVALID_SHAPE`(너무 작거나 큰 도형) / `CREATED`(빈 구역이라 새 땅 생성) / `INTERACTED`(기존 땅과 겹쳐 데미지·회복·점령 발생) |
| createdTerritorySno | `outcome=CREATED`일 때 새로 만든 `territory.sno` |
| capturedTerritories | HP를 0으로 만들어 이번 러닝으로 뺏어온 남의 땅 목록. 각 원소에 `territorySno`, `previousOwnerMemberSno`, `previousOwnerNickname` |
| damagedTerritorySnos | 데미지만 주고 점령까지는 못 한 땅 `sno` 목록 |
| healedTerritorySnos | 내 소유 땅 중 이번 러닝으로 HP를 회복시킨 땅 `sno` 목록 |

**멱등성**: 마지막으로 저장된 좌표 시각(`last_point_at`) 이후 좌표만 저장하므로, 같은 청크를 다시 보내도 `skippedPoints`로 집계될 뿐 중복 저장되지 않습니다. 이미 `FINISHED`된 러닝에 청크가 또 와도 무시하고 확정된 결과만 돌려줍니다.

**동작**
- 첫 청크: `record_track` 행 생성(`status=ACTIVE`, `record_sno=null`), 좌표를 `points_json`에 저장
- 이후 청크: 워터마크 이후 좌표만 `points_json`에 append, 거리·좌표수·마지막 좌표 갱신
- `finished: true`: 누적 거리·시작/종료 시각으로 `RecordCreateRequest`를 만들어 **기존 `POST /api/record/save`와 동일한 저장·점수 산정 로직**(`RecordService.create` — 거리/페이스 상한 검증, 최근 1개월 내 동일 `startTime` 중복 거부, `Rank`/`MemberScore` 반영)을 재사용하고, `record_track.status=FINISHED` + `record_sno` 채움
- 앱이 `finished: true`를 못 보내고 끊긴 경우: `GpsSessionSweeper`(스케줄러, 기본 5분 주기)가 마지막 청크 후 **30분(`paceleague.gps.sweeper.idle-minutes`) 넘게 조용한 `ACTIVE` 세션**을 쌓인 좌표로 자동 마감합니다. 좌표가 없거나 거리/페이스가 비정상이라 기록을 만들 수 없으면 `status=ABANDONED`로 두고 재시도하지 않습니다

**실패**
- `clientRunId` 누락/100자 초과, `activityType != RUNNING`, 좌표 없는데 `finished`도 아님, 한 청크 2,000개 초과, `recordedAt` 누락, 좌표 범위(위도 ±90 / 경도 ±180) 이탈, 러닝 전체 60,000개 초과 → 400
- `finished: true` 인데 세션에 저장된 좌표가 하나도 없음 → 400
- 종료 시 거리/페이스가 러닝 기록으로 불가능한 값, 최근 1개월 내 동일 `startTime` 존재 → 400 (`RecordService.create`의 기존 규칙). 이 경우 트랜잭션이 롤백되어 세션은 `ACTIVE`로 남고, 앱이 `finished: true`를 재시도할 수 있음

**마이그레이션**: [migrations/2026-08-27_record_gps_track.sql](./migrations/2026-08-27_record_gps_track.sql) → [migrations/2026-08-27_record_track_streaming.sql](./migrations/2026-08-27_record_track_streaming.sql) 순서로 (운영은 배포 전 직접 실행)

---

## Territory API (`/api/territory`) — 러닝 땅따먹기

러닝 GPS 경로가 이룬 닫힌 도형을 "땅"으로 저장하고, 겹치는 러닝으로 데미지를 주고받는 게임 기능(2026-08-27 1차 구현). 도메인 로직은 [domains.md](./domains.md#러닝-땅따먹기territory-도메인) 참고.

땅 생성/데미지/점령은 **별도 엔드포인트가 아니라** `POST /api/record/gps`의 러닝 종료 시점에 일어난다 — 그 러닝이 `territoryMode: true`로 시작한 세션일 때만. 아래는 그 결과를 지도에 보여주는 조회 엔드포인트.

### GET `/api/territory/map` — 지도 영역 내 땅 조회 (공개, 인증 불필요)

지도가 보고 있는 영역(bounds)과 줌 레벨로 점령된 땅 목록을 반환합니다. `web/territory.html`(Google Maps JS API)이 폴리곤으로 그립니다. 로그인 상태로 호출하면 각 땅의 `mine` 플래그가 채워집니다.

**Query params**

| 파라미터 | 타입 | 설명 |
|---|---|---|
| swLat, swLng | double | 지도 남서쪽 모서리 위경도. **필수** |
| neLat, neLng | double | 지도 북동쪽 모서리 위경도. **필수** |
| zoom | int | 지도 줌 레벨. **필수**. `paceleague.territory.min-zoom`(기본 13) 미만이면 빈 목록 + `zoomTooLow: true` (데이터 과다 방지) |
| lang | string | 티어 라벨 언어(`ko`/`en`/`ja`/`zh`/`es`/`fr`/`de`/`pt`/`vi`/`th`), 미지원 값이면 `ko`. 기본 `ko` |
| country | string | ISO 3166-1 alpha-2 국가코드(예: `KR`). 주어지면 `lang` 대신 이 국가에 맞는 언어로 응답 |

**Response** `200 OK` — `data`: `TerritoryMapResponse`

```json
{ "zoomTooLow": false, "minZoom": 13,
  "territories": [
    { "sno": 12, "polygon": [[37.5665,126.978],[37.5665,126.979],[37.5673,126.979],[37.5673,126.978],[37.5665,126.978]],
      "centerLat": 37.5669, "centerLng": 126.9785,
      "ownerNickname": "달리는곰", "ownerTier": "GOLD", "ownerTierLabel": "골드",
      "hp": 70, "maxHp": 100, "mine": false }
  ] }
```

| 필드 | 설명 |
|---|---|
| zoomTooLow | `true`이면 `territories`는 항상 빈 목록. 클라이언트는 "지도를 더 확대하세요" 안내를 표시 |
| polygon | `[[lat,lng], ...]` 위/경도 링(실제 러닝 경로 기반) |
| ownerTier / ownerTierLabel | 소유자의 현재 시즌 티어 enum / 언어별 라벨. 점수 없으면 `SILVER` |
| hp / maxHp | 현재 체력 / 최대 체력. 겹치는 러닝에 데미지를 입고 0이 되면 소유권이 넘어감 |
| mine | 호출자 소유 여부. 비로그인이면 항상 `false` |

### GET `/api/territory/ranking` — 면적 기준 땅따먹기 랭킹 (공개, 인증 불필요)

소유자별 **총 점령 면적(m²) 내림차순** 랭킹. `web/territory.html` 지도 우상단 "랭킹" 패널이 사용. 로그인 상태로 호출하면 본인 항목에 `mine: true`.

**Query params**

| 파라미터 | 타입 | 설명 |
|---|---|---|
| lang | string | 티어 라벨 언어(`ko`/`en`/`ja`/`zh`/`es`/`fr`/`de`/`pt`/`vi`/`th`), 미지원 값이면 `ko`. 기본 `ko` |
| country | string | ISO 3166-1 alpha-2 국가코드(예: `KR`). 주어지면 `lang` 대신 이 국가에 맞는 언어로 응답 |

최대 항목 수는 `paceleague.territory.ranking-max-results`(기본 100).

**Response** `200 OK` — `data`: `TerritoryRankingResponse`

```json
{ "entries": [
    { "rank": 1, "memberSno": 42, "nickname": "달리는곰",
      "ownerTier": "GOLD", "ownerTierLabel": "골드",
      "totalAreaSqm": 152340.5, "territoryCount": 4, "mine": false }
  ] }
```

| 필드 | 설명 |
|---|---|
| rank | 1부터. `totalAreaSqm` 내림차순 |
| totalAreaSqm | 해당 소유자가 가진 ACTIVE 땅들의 면적 합(m²) |
| territoryCount | 보유 중인 ACTIVE 땅 개수 |
| mine | 호출자 본인 항목 여부. 비로그인이면 항상 `false` |

**마이그레이션**: [migrations/2026-08-27_territory_feature.sql](./migrations/2026-08-27_territory_feature.sql) (운영은 배포 전 직접 실행. `record_track.territory_mode` 컬럼 + `territory`/`territory_contribution` 테이블). 랭킹은 기존 `territory` 테이블만 조회하므로 추가 마이그레이션 없음.

---

## Crew API (`/api/crew`) — 크루(길드)

크루 생성/검색/초대/가입신청/크루원 관리(2026-08-28 1단계). 도메인 규칙은 [domains.md](./domains.md#크루crew-도메인) 참고. **크루 검색만 공개**, 나머지는 전부 로그인 필요. `web/crew.html` 이 이 API를 사용.

전제: **한 회원 = 한 크루**. 가입 방식은 승인제만. 알림 시스템이 없어 초대받은 회원은 `GET /api/crew/invitations/me` 로 확인.

| 메서드 · 경로 | 설명 |
|---|---|
| `POST /api/crew` | 크루 생성 `{ name, iconMediaId?, description? }`. 크루 없는 회원만. 생성자가 크루장+첫 크루원 |
| `GET /api/crew/search?q=` | **공개**. 크루명 부분 일치(q 없으면 이름순 목록). 공개 정보만 |
| `GET /api/crew/me` | 내 크루 상세. 없으면 `data: null` |
| `GET /api/crew/{crewSno}` | 크루 상세. 요청자가 크루원이면 `notice`+`members`(닉네임·티어) 포함, 아니면 공개 정보만 (`viewerIsMember`/`viewerIsLeader`) |
| `PUT /api/crew/{crewSno}` | 크루장: `{ name, iconMediaId?, iconUrl?, description?, notice? }` 통째 갱신. 아이콘 미변경 시 현재 `iconUrl` 을 그대로 echo |
| `DELETE /api/crew/{crewSno}` | 크루장: 해체(혼자 남았을 때만). 관련 데이터 전부 하드 삭제 |
| `DELETE /api/crew/{crewSno}/members/me` | 크루원 탈퇴. 크루장은 위임/해체 먼저 |
| `DELETE /api/crew/{crewSno}/members/{targetMemberSno}` | 크루장: 추방 |
| `POST /api/crew/{crewSno}/leader` `{ inviteeMemberSno }` | 크루장 위임(기존 크루장은 일반 크루원) |
| `POST /api/crew/{crewSno}/invitations` `{ inviteeMemberSno }` | 크루장: 회원 초대 |
| `GET /api/crew/invitations/me` | 내가 받은 PENDING 초대 목록(만료분 제외) |
| `POST /api/crew/invitations/{id}/accept` \| `/decline` | 초대 수락(→ 가입) / 거절 |
| `DELETE /api/crew/invitations/{id}` | 크루장: 보낸 초대 취소 |
| `POST /api/crew/{crewSno}/join-requests` `{ message? }` | 회원: 가입신청 |
| `GET /api/crew/{crewSno}/join-requests` | 크루장: 대기 중 신청 목록 |
| `POST /api/crew/join-requests/{id}/approve` \| `/reject` | 크루장: 승인(→ 가입) / 거절 |
| `DELETE /api/crew/join-requests/{id}` | 신청자: 신청 취소 |
| `GET /api/crew/{crewSno}/ranking` | 크루원만. 크루 내 회원을 현재 시즌 점수 기준 내림차순 (2단계) |

**크루 배지 (2단계)**: 크루 소속 회원은 다른 응답에도 크루명·아이콘이 붙는다 — `board`의 `PostSummaryResponse`/`PostDetailResponse`에 `authorCrewName`/`authorCrewIconUrl`, `ranking`의 `RankingUserResponse`(`getRanking`/`top10`)에 `crewName`/`crewIconUrl` (크루 없으면 `null`).

초대 대상은 [GET /api/member/search](#get-apimembersearch--회원-검색-인증-필요) 로 찾는다.

**실패**: 이름 길이/중복, 이미 크루 소속, 정원 초과, 크루장 아님, 만료된 초대, 크루원 남은 상태에서 해체 등 → 400.

**마이그레이션**: [migrations/2026-08-28_crew_feature.sql](./migrations/2026-08-28_crew_feature.sql) (운영은 배포 전 직접 실행. `crew`/`crew_member`/`crew_invitation`/`crew_join_request` 4개 테이블)

---

## Rank API (`/api/rank`) — 인증 필요

"내 점수/티어"를 조회하는 개인 관점 API. 리더보드는 [Ranking API](#ranking-api-apiranking--인증-필요) 참고.

### GET `/api/rank/me` — 내 랭크 조회

현재 시즌 기준 내 누적 점수, 현재 티어, 다음 티어까지 남은 점수를 반환합니다.

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| lang | string (`ko`\|`en`\|`ja`\|`zh`\|`es`\|`fr`\|`de`\|`pt`\|`vi`\|`th`) | `ko` | 티어 라벨(`currentTierLabel`/`nextTierLabel`) 표시 언어. 미지원 값은 `ko`로 처리(400 아님) |
| country | string (ISO 3166-1 alpha-2, 예: `KR`) | - | 주어지면 `lang` 대신 이 국가에 맞는 언어로 응답(`common.i18n.CountryLanguageResolver`로 변환, 매핑에 없으면 `en`) |

**Response** `200 OK` — `data`: `RankMeResponse`

```json
{
  "totalScore": 1620,
  "currentTier": "SILVER",
  "currentTierLabel": "Silver",
  "nextTier": "GOLD",
  "nextTierLabel": "Gold",
  "nextTierRequiredScore": 3000,
  "remainingScore": 1380
}
```

- 이번 시즌에 아직 기록이 없는 회원은 기본값(`totalScore = 1500`, `currentTier = SILVER`)으로 응답합니다.
- `nextTier`/`nextTierLabel`/`nextTierRequiredScore`/`remainingScore`는 최고 티어(`CHALLENGER`)에서는 각각 `null`/`null`/`0`/`0`.
- `currentTier`/`nextTier`는 로직/필터링용 원본 enum 코드로 언어와 무관하게 항상 동일. `currentTierLabel`/`nextTierLabel`이 `lang`에 따라 달라지는 화면 표시용 문자열이며, `rank.domain.policy.RankTierLabelPolicy`의 고정 번역 테이블에서 조회한다(AWS Translate 미사용 — 티어명 7개는 값이 고정돼 있어 굳이 API 호출할 이유가 없음).
- 티어 구간은 [domains.md](./domains.md#티어-rank-tier) 참고.

---

## Ranking API (`/api/ranking`)

전체 리더보드(Top3) + 내 주변 순위를 함께 반환하는 API. `top10`만 예외적으로 공개(인증 불필요) 엔드포인트입니다.

### GET `/api/ranking/getRanking` — 랭킹 페이지 조회 (인증 필요)

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| lang | string (`ko`\|`en`\|`ja`\|`zh`\|`es`\|`fr`\|`de`\|`pt`\|`vi`\|`th`) | `ko` | 각 항목의 `tierLabel` 표시 언어. 미지원 값은 `ko`로 처리 |
| country | string (ISO 3166-1 alpha-2, 예: `KR`) | - | 주어지면 `lang` 대신 이 국가에 맞는 언어로 응답 |

**Response** `200 OK` — `data`: `RankingPageResponse`

```json
{
  "topRanks": [
    { "rank": 1, "memberSno": 7, "nickname": "페이서", "totalScore": 24500, "tier": "CHALLENGER", "tierLabel": "Challenger", "me": false },
    { "rank": 2, "memberSno": 3, "nickname": "런닝맨", "totalScore": 19800, "tier": "MASTER", "tierLabel": "Master", "me": false },
    { "rank": 3, "memberSno": 1, "nickname": "러너01", "totalScore": 15200, "tier": "MASTER", "tierLabel": "Master", "me": true }
  ],
  "aroundRanks": [
    { "rank": 1, "memberSno": 1, "nickname": "러너01", "totalScore": 15200, "tier": "MASTER", "tierLabel": "Master", "me": true },
    { "rank": 2, "memberSno": 9, "nickname": "..." , "totalScore": 14990, "tier": "MASTER", "tierLabel": "Master", "me": false }
  ]
}
```

- `topRanks`: 시즌 전체 1~3위 (항상 최대 3명)
- `aroundRanks`: 내 순위를 중심으로 앞뒤 포함 **5명** (내 순위가 3위 이하로 밀려있어도 상위 유저부터 잘리지 않도록 `offset = max(0, 내 순위 - 3)`으로 계산)
- 두 목록 모두 각 항목에 `me: true/false`로 본인 여부 표시
- `tier`는 원본 enum 코드(로직/필터링용, 언어 무관), `tierLabel`은 `lang`에 따라 달라지는 화면 표시용 문자열(`rank.domain.policy.RankTierLabelPolicy`)
- 순위 산정 로직(동점자 처리 포함)은 [domains.md](./domains.md#랭킹리더보드-산정-로직) 참고

### GET `/api/ranking/top10` — 상위 10명 랭킹 조회 (공개, 인증 불필요)

`paceleague.co.kr` 랜딩 페이지(`web/index.html`)에서 표시하기 위한 공개 엔드포인트입니다. 시즌 상위 10명을 그대로 반환합니다 (내부적으로 `getRanking`의 "내 주변 순위" 조회와 같은 쿼리를 `offset=0, limit=10`으로 재사용).

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| lang | string (`ko`\|`en`\|`ja`\|`zh`\|`es`\|`fr`\|`de`\|`pt`\|`vi`\|`th`) | `ko` | 각 항목의 `tierLabel` 표시 언어. 미지원 값은 `ko`로 처리 |
| country | string (ISO 3166-1 alpha-2, 예: `KR`) | - | 주어지면 `lang` 대신 이 국가에 맞는 언어로 응답 |

**Response** `200 OK` — `data`: `RankingUserResponse[]`

```json
[
  { "rank": 1, "memberSno": 7, "nickname": "페이서", "totalScore": 24500, "tier": "CHALLENGER", "tierLabel": "Challenger", "me": false },
  { "rank": 2, "memberSno": 3, "nickname": "런닝맨", "totalScore": 19800, "tier": "MASTER", "tierLabel": "Master", "me": false }
]
```

- 로그인 컨텍스트가 없으므로 `me`는 항상 `false`.
- CORS: `https://paceleague.co.kr`, `https://www.paceleague.co.kr` 오리진에서만 브라우저 `fetch`로 호출 가능하도록 이 경로에만 한정해 CORS를 허용합니다 (`common.config.CorsConfig`). 다른 API는 CORS를 열지 않았으므로 브라우저에서 다른 오리진으로는 호출할 수 없습니다. `lang`은 일반 쿼리 파라미터라 CORS 프리플라이트에 영향 없음(커스텀 헤더가 아님).

---

## Locale API (`/api/common`) — 공개

국가 코드로 이 서비스가 지원하는 언어를 판별하는 유틸리티 API. 특정 도메인에 속하지 않는 순수 조회 기능이라 `common` 패키지에 있음 — 자세한 설계 배경은 [architecture.md](./architecture.md#정적-ui-라벨-다국어i18n--2026-08-11-추가) 참고.

> **참고**: 아래 `GET /api/common/language`는 언어 코드만 반환하는 별도 조회용 엔드포인트다. 실제 카테고리명/티어 라벨 데이터를 국가 기준으로 바로 받고 싶다면 이 엔드포인트를 먼저 호출할 필요 없이, `lang`을 받는 각 엔드포인트(Board/Rank/Ranking API)에 `country`를 직접 넘기면 된다 — 두 파라미터를 모두 주면 `country`가 우선한다(`common.i18n.LocaleResolver`).

### GET `/api/common/language` — 국가 코드로 언어 조회 (공개, 인증 불필요)

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| country | string (ISO 3166-1 alpha-2, 예: `KR`, `US`, `JP`) | N | 대소문자 무관. 생략하거나 매핑에 없는 국가면 `en` 반환 |

**Response** `200 OK` — `data`: `{ "language": "ko" }`

```json
{ "language": "ko" }
```

- 반환되는 `language`는 board/rank/ranking API들의 `lang` 쿼리 파라미터에 그대로 넣어 쓸 수 있는 10개 코드(`ko`/`en`/`ja`/`zh`/`es`/`fr`/`de`/`pt`/`vi`/`th`) 중 하나.
- `country`가 비어있거나 매핑 테이블(`common.i18n.CountryLanguageResolver`)에 없는 국가면 `en`을 기본값으로 반환한다 — 잘못된 값이어도 400을 던지지 않고 항상 200으로 응답(관대한 폴백, `Language.fromCode`가 미지원 언어 코드를 `ko`로 조용히 폴백하는 것과 같은 스타일).
- 웹(`web/js/i18n.js`)은 이미 브라우저 `navigator.language`/localStorage 기반으로 자체 언어를 판별하므로 이 엔드포인트를 호출하지 않는다 — 국가 기반 판별이 필요한 다른 클라이언트(모바일 앱 등)를 위한 API.

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

## Media API (`/api/media`) — 인증 필요

게시글에 첨부할 이미지/동영상/링크를 관리하는 API. S3 presigned URL로 브라우저가 파일을 직접 업로드하고(백엔드는 파일 바이트를 프록시하지 않음), AWS Rekognition으로 유해 콘텐츠 여부를 모더레이션합니다. 인프라(S3 버킷/IAM) 배경은 [infra.md](./infra.md), 스키마는 [database.md](./database.md#media) 참고.

전체 흐름: `POST /uploads`로 presigned URL 발급 → 브라우저가 그 URL에 파일을 직접 `PUT` → `POST /{mediaSno}/complete`로 완료 통보(이미지는 즉시 결과, 동영상은 `PENDING`) → 동영상이면 `GET /{mediaSno}/status`를 폴링 → `APPROVED`된 `mediaSno`만 게시글 작성 시 `attachmentMediaIds`로 전달. 링크는 업로드 없이 `POST /links`로 바로 생성됩니다.

### POST `/api/media/uploads` — 업로드 URL 발급 (인증 필요)

```json
{ "type": "IMAGE", "mimeType": "image/jpeg", "fileSizeBytes": 2048000 }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| type | `IMAGE`\|`VIDEO` | `LINK`는 이 엔드포인트를 쓰지 않음(아래 `/links` 참고) |
| mimeType | string | 이미지: `image/jpeg`\|`image/png`\|`image/webp`\|`image/gif`, 동영상: `video/mp4`\|`video/quicktime`만 허용 |
| fileSizeBytes | number | 클라이언트가 신고하는 파일 크기(신고값 기준 1차 검증 — 실제 크기는 `/complete`에서 S3 `HeadObject`로 재검증) |

**Response** `200 OK` — `data`: `{ "mediaSno": 11, "uploadUrl": "https://paceleague-media.s3.ap-northeast-2.amazonaws.com/...", "expiresInSeconds": 300 }`

`uploadUrl`은 5분간 유효한 presigned `PUT` URL입니다. 클라이언트는 이 URL에 `Content-Type` 헤더를 업로드 요청과 동일하게 맞춰서 파일 바이트를 그대로 `PUT`해야 합니다(서명에 포함된 값과 다르면 S3가 `SignatureDoesNotMatch`로 거부).

**실패**: `type`이 `IMAGE`/`VIDEO`가 아님 → 400, `mimeType`이 허용 목록에 없음 → 400, `fileSizeBytes`가 이미지 10MB/동영상 200MB 초과 → 400

### POST `/api/media/{mediaSno}/complete` — 업로드 완료 처리 (인증 필요)

S3에 실제 업로드가 끝난 뒤 호출합니다. 본인 소유가 아니거나 존재하지 않는 `mediaSno` → 400. 이미 처리된(`APPROVED`/`REJECTED`) 미디어에 다시 호출하면 그 상태를 그대로 반환합니다(멱등).

**Response** `200 OK` — `data`: `MediaStatusResponse`

```json
{ "mediaSno": 11, "status": "APPROVED", "url": "https://paceleague-media.s3.ap-northeast-2.amazonaws.com/media/image/5/....jpg", "moderationReason": null }
```

- 실제 업로드된 파일 크기가 제한(이미지 10MB/동영상 200MB)을 넘으면 S3 객체를 삭제하고 `REJECTED`
- `IMAGE`는 Rekognition `DetectModerationLabels`를 동기 호출해 이 응답에서 바로 `APPROVED`/`REJECTED`가 결정됨
- `VIDEO`는 Rekognition `StartContentModeration`(비동기 작업)만 시작하고 `status: "PENDING"`으로 응답 — `GET /{mediaSno}/status`로 폴링해야 함
- `REJECTED`(모더레이션 거부/용량 초과 모두)면 `moderationReason`에 감지된 라벨명(최대 500자)이 채워지고, `url`은 계속 `null`이며 S3 객체 자체도 즉시 삭제됨(공개 버킷에 유해 콘텐츠가 남아있지 않도록)

**실패**: `mediaSno`가 없거나 본인 소유가 아님 → 400, `LINK` 타입에 호출 → 400, 아직 S3에 파일이 업로드되지 않은 상태(presigned URL로 PUT하기 전에 호출) → 400

### GET `/api/media/{mediaSno}/status` — 업로드/모더레이션 상태 폴링 (인증 필요)

동영상처럼 `PENDING` 상태인 미디어의 진행 상황을 확인합니다. 호출 시점에 Rekognition 비동기 작업이 끝나 있으면(`GetContentModeration` 재조회) 그 결과로 상태가 갱신되어 응답됩니다. 응답 형식은 `/complete`와 동일한 `MediaStatusResponse`.

**실패**: `mediaSno`가 없거나 본인 소유가 아님 → 400

### POST `/api/media/links` — 링크 첨부 생성 (인증 필요)

```json
{ "url": "https://example.com" }
```

업로드/모더레이션 없이 URL만 저장하고 생성 즉시 `APPROVED`됩니다. `http://`/`https://`로 시작하는 URL만 허용됩니다(`javascript:` 등 다른 스킴은 거부 — 프론트엔드가 이 URL을 그대로 `<a href>`에 렌더링하므로 XSS 방지 목적).

**Response** `200 OK` — `data`: `mediaSno` (숫자)

**실패**: `url`이 비어있거나 `http(s)://`로 시작하지 않음 → 400, 1000자 초과 → 400

### 게시글 첨부까지의 제한값

| 항목 | 값 |
|---|---|
| 게시글당 최대 첨부 개수 | 10개(`attachmentMediaIds`) |
| 이미지 허용 형식/최대 용량 | jpeg/png/webp/gif, 10MB |
| 동영상 허용 형식/최대 용량 | mp4/quicktime(mov), 200MB |
| Rekognition 모더레이션 임계값 | 라벨 신뢰도(confidence) 60 이상이면 거부 |
| presigned 업로드 URL 만료 | 5분 |

## Board API (`/api/board`) — 조회는 공개, 작성/삭제/추천은 인증 필요

커뮤니티(보드/게시글/댓글/추천). 레딧처럼 **조회(GET)는 비로그인도 가능**하고, 글/댓글 작성·삭제·추천처럼 쓰기 작업(POST/DELETE)만 로그인이 필요합니다. 웹(`paceleague.co.kr`/`www.paceleague.co.kr`)에서 브라우저로 직접 호출하므로 CORS가 열려 있습니다(`CorsConfig`의 `/api/board/**` 등록, [architecture.md](./architecture.md) 참고).

비로그인으로 조회하면 `myVote`는 항상 `null`입니다(내 투표 여부를 알 수 없으므로).

댓글은 **1단계 중첩만 허용**됩니다 — 최상위 댓글에는 답글을 달 수 있지만, 답글에는 답글을 달 수 없습니다(`parentCommentSno`가 이미 있는 댓글을 다시 `parentCommentSno`로 지정하면 400).

추천/비추천은 토글 방식입니다: 같은 값으로 다시 요청하면 추천 취소, 다른 값으로 요청하면 전환, 처음이면 신규 생성.

### GET `/api/board` — 보드 목록 조회 (공개)

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| lang | string (`ko`\|`en`\|`ja`\|`zh`\|`es`\|`fr`\|`de`\|`pt`\|`vi`\|`th`) | `ko` | `name`/`description` 표시 언어. 미지원 값은 `ko`로 처리 |
| country | string (ISO 3166-1 alpha-2, 예: `KR`) | - | 주어지면 `lang` 대신 이 국가에 맞는 언어로 응답 |

**Response** `200 OK` — `data`: `BoardResponse[]`

```json
[
  { "sno": 1, "slug": "free", "name": "자유게시판", "description": "자유롭게 이야기하는 공간입니다." }
]
```

- `slug`는 언어와 무관하게 항상 고정(URL/로직용 식별자), `name`/`description`만 `lang`에 따라 번역되어 내려간다(`board.domain.policy.BoardLabelPolicy`의 고정 번역 테이블 — 보드가 3개뿐이라 AWS Translate 대신 정적 테이블 사용). `lang=ko`이거나 정책에 없는 신규 보드/slug면 DB에 저장된 원본 값을 그대로 반환한다.

### GET `/api/board/{boardSno}/posts` — 게시글 목록 조회 (공개)

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| page | int | 0 | |
| size | int | 20 | |
| sort | string (`new`\|`top`) | `new` | `top`은 `score DESC, create_at DESC` |
| lang | string (`ko`\|`en`\|`ja`\|`zh`\|`es`\|`fr`\|`de`\|`pt`\|`vi`\|`th`) | `ko` | `authorTierLabel` 표시 언어. 미지원 값은 `ko`로 처리 |
| country | string (ISO 3166-1 alpha-2, 예: `KR`) | - | 주어지면 `lang` 대신 이 국가에 맞는 언어로 응답 |

**Response** `200 OK` — `data`: Spring `Page<PostSummaryResponse>` (`sno`, `title`, `nickname`, `authorTier`, `authorTierLabel`, `recordSno`, `contentSnippet`, `thumbnailUrl`, `thumbnailType`, `viewCount`, `score`, `commentCount`, `createAt`)

- `authorTier`: 작성자의 현재 시즌 티어(`RankTier` enum, `rank.GetMemberTierPort`로 조회, 이번 시즌 기록이 없으면 기본값 `SILVER`) — 언어와 무관한 원본 코드
- `authorTierLabel`: `authorTier`를 `lang`에 맞게 번역한 화면 표시용 문자열(`rank.domain.policy.RankTierLabelPolicy`)
- `recordSno`: 작성 시 첨부한 기록의 PK(없으면 `null`). 목록에서는 첨부 여부 표시용으로만 쓰고, 기록 상세 내용은 게시글 상세 조회에서만 내려줌
- `contentSnippet`: 게시글 `content`(sanitize된 HTML)에서 태그를 뺀 순수 텍스트를 최대 200자로 잘라 내려주는 목록용 본문 미리보기(`PostContentSanitizer.snippet`). 이미지/동영상만 있고 텍스트가 없으면 빈 문자열(`""`) — 웹 클라이언트는 이 경우 스니펫 영역 자체를 숨김.
- `thumbnailUrl`/`thumbnailType`(`IMAGE`\|`VIDEO`, 없으면 둘 다 `null`): 게시글 `content` HTML 안에서 **가장 먼저 등장하는** 이미지/동영상 하나를 서버가 정규식으로 뽑아 목록용 썸네일로 내려줌(`board.domain.policy.PostContentSanitizer.firstMediaPreview`) — 미디어 테이블을 다시 조회하지 않고 이미 응답에 포함된 `content`에서 바로 추출하는 방식. 2026-08-11 이전에는 `attachmentCount`(첨부 개수)였으나, 웹 에디터가 이미지/동영상을 `content`에 인라인으로 삽입하는 방식으로 바뀌면서 `attachmentMediaIds`로 명시 연결되는 미디어가 웹에서는 사실상 없어져 이 필드가 항상 0이 되는 문제가 있어 교체함.
- 웹 클라이언트(`web/index.html`)는 카드 하나에 제목 → `contentSnippet`(최대 3줄, 넘치면 말줄임) → 썸네일(카드 폭 100%, 최대 높이 360px, `object-fit: cover`로 비율 유지하며 채움) → 메타(닉네임/티어/추천/댓글/조회/시간) 순서로 렌더링(레딧 카드뷰 벤치마킹).

**실패**: 존재하지 않는 `boardSno` → 400

### POST `/api/board/{boardSno}/posts` — 게시글 작성 (인증 필요)

```json
{ "title": "오늘 10km 완주!", "content": "<p>날씨가 좋아서 기분 좋게 뛰었습니다.</p><img src=\"https://paceleague-media.s3.ap-northeast-2.amazonaws.com/media/image/5/....jpg\">", "recordSno": 123, "attachmentMediaIds": null }
```

`content`는 **서버에서 sanitize된 HTML**로 저장됩니다(2026-08-11부터 — 그 전에는 순수 텍스트였음). 웹 클라이언트(`web/index.html`)는 굵게/기울임/링크/이미지/동영상을 지원하는 인라인 에디터(`contenteditable`)로 작성하며, 제출 시 에디터의 `innerHTML`을 그대로 보냅니다. 서버(`board.domain.policy.PostContentSanitizer`, OWASP Java HTML Sanitizer 기반)는 `p, br, div, b, strong, i, em, a[href], img[src,alt], video[src,controls]`만 화이트리스트로 허용하고 그 외(`script`, `style`, `on*` 속성, `class`, `javascript:` URL 등)는 전부 제거합니다 — 저장형 XSS 방지가 목적이며, 이 서버측 sanitize가 유일한 신뢰 경계입니다.

`content`가 "비어있는지" 판정은 sanitize 후 태그를 뺀 순수 텍스트 기준이며, 텍스트가 없어도 `<img>`/`<video>`가 하나라도 있으면 유효한 게시글로 인정합니다(이미지/동영상만 있는 글 허용).

`recordSno`는 선택 항목입니다. 지정하면 본인 소유 기록인지 검증합니다(`GET /api/record/recent-30-days` 등으로 조회한 본인 기록의 `recordSno`만 사용 가능 — 기간 제한은 없고, 다른 회원 소유이거나 존재하지 않는 `recordSno`면 거부됩니다).

`attachmentMediaIds`도 선택 항목입니다(최대 10개). **웹 에디터는 더 이상 이 필드를 쓰지 않습니다** — 이미지/동영상을 에디터에 삽입하면 승인된 URL이 `content`의 `<img>`/`<video>` 태그 안에 바로 박히기 때문입니다. 이 필드는 [Media API](#media-api-apimedia--인증-필요)로 미리 업로드/모더레이션까지 끝낸(`APPROVED` 상태) 본인 소유 `mediaSno`를 게시글과 명시적으로 연결하고 싶은 다른 클라이언트(모바일 앱 등)를 위해 API에는 남아 있습니다 — 아직 다른 게시글에 첨부되지 않은 것이어야 합니다.

**Response** `200 OK` — `data`: `{ "sno": 123 }`

**실패**: `title` 공백 또는 200자 초과 → 400, `content`가 (원본 기준) 10,000자 초과 → 400, sanitize 후 텍스트도 이미지/동영상도 없음(실질적으로 빈 본문) → 400, 존재하지 않는 `boardSno` → 400, `recordSno`가 존재하지 않거나 본인 소유가 아님 → 400 `"record not found"`, `attachmentMediaIds`가 10개 초과 → 400, 존재하지 않거나/본인 소유가 아니거나/`APPROVED`가 아니거나/이미 다른 게시글에 첨부된 `mediaSno`가 섞여 있으면 → 400(이 경우 게시글 자체도 저장되지 않음 — 같은 트랜잭션에서 롤백)

### PUT `/api/board/posts/{postSno}` — 게시글 수정 (인증 필요)

요청 본문/검증 규칙은 작성(`POST`)과 완전히 동일합니다(`PostCreateRequest` 재사용 — 제목/본문/`recordSno`/`attachmentMediaIds` 전부 다시 보내야 하며, 부분 수정(PATCH)이 아닙니다). 본인 게시글만 수정 가능합니다.

```json
{ "title": "오늘 10km 완주! (수정)", "content": "<p>날씨가 좋아서 기분 좋게 뛰었습니다.</p>", "recordSno": 123, "attachmentMediaIds": null }
```

`attachmentMediaIds`는 **이번 수정에서 새로 업로드한 미디어**만 나열합니다 — 기존에 본문 HTML 안에 이미 인라인으로 박혀 있던 이미지/동영상은 `content` 문자열 자체에 URL이 그대로 남아있으므로 다시 첨부할 필요가 없습니다.

**Response** `200 OK` — `data`: `"게시글이 수정되었습니다."`

**실패**: 존재하지 않거나 본인 소유가 아님 → 400 `"post not found"`, 그 외 검증 실패는 작성 API와 동일

### GET `/api/board/posts/{postSno}` — 게시글 상세 조회 (공개)

조회할 때마다 `view_count`가 1 증가합니다(중복 방지 없음). `myVote`는 내가 이 글에 투표한 값(`1`/`-1`/`null`), 비로그인이면 항상 `null`.

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| lang | string (`ko`\|`en`\|`ja`\|`zh`\|`es`\|`fr`\|`de`\|`pt`\|`vi`\|`th`) | `ko` | `boardName`/`authorTierLabel` 표시 언어. 미지원 값은 `ko`로 처리 |
| country | string (ISO 3166-1 alpha-2, 예: `KR`) | - | 주어지면 `lang` 대신 이 국가에 맞는 언어로 응답 |

**Response** `200 OK` — `data`: `PostDetailResponse`

```json
{
  "sno": 123, "boardSno": 1, "boardName": "자유게시판",
  "title": "오늘 10km 완주!",
  "content": "<p>날씨가 좋아서 기분 좋게 뛰었습니다.</p><img src=\"https://paceleague-media.s3.ap-northeast-2.amazonaws.com/media/image/5/....jpg\">",
  "memberSno": 5, "nickname": "러너1", "authorTier": "GOLD", "authorTierLabel": "Gold",
  "attachedRecord": {
    "recordSno": 456, "startTime": "2026-08-08T06:30:00", "endTime": "2026-08-08T07:05:00",
    "distance": 10230.5, "createAt": "2026-08-08T07:05:10"
  },
  "attachments": [
    { "mediaSno": 11, "type": "IMAGE", "url": "https://paceleague-media.s3.ap-northeast-2.amazonaws.com/media/image/5/....jpg" }
  ],
  "viewCount": 12, "score": 3, "myVote": 1,
  "createAt": "2026-08-08T10:00:00", "updateAt": "2026-08-08T10:00:00"
}
```

- `boardName`: 목록 조회(`GET /api/board`)와 동일한 번역 테이블로 `lang`에 맞게 번역됨
- `authorTier`/`authorTierLabel`: 목록 조회와 동일 — `authorTier`는 원본 코드, `authorTierLabel`이 번역된 표시 문자열
- `content`: 서버에서 sanitize된 HTML(위 작성 API 설명 참고). 클라이언트는 이 값을 `innerHTML`로 그대로 렌더링해도 되는 것을 서버가 보장함(sanitize가 쓰기 시점 1곳에서만 이뤄지는 게 이 API의 신뢰 경계).
- `attachedRecord`: 첨부한 기록이 없으면 `null`. 첨부된 기록이 이후 삭제된 경우에도 `null`로 응답(참조 무결성을 강제하지 않음)
- `attachments`: `attachmentMediaIds`로 명시적으로 연결된 첨부 목록(없으면 빈 배열) — **웹 클라이언트는 더 이상 이 필드를 화면에 쓰지 않습니다**(이미지/동영상이 이미 `content` 안에 인라인으로 포함돼 있어서 별도로 그리면 중복 표시됨). 다른 클라이언트를 위해 API에는 계속 남아 있음 — [Media API](#media-api-apimedia--인증-필요) 참고

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

`content`는 항상 **평문**입니다(원본이 HTML이어도 태그를 전부 제거한 뒤 번역하고, 번역 결과도 평문 그대로 반환 — AWS Translate가 HTML을 이해하지 못해 태그가 섞이면 번역이 깨지기 때문). 이미지/동영상만 있고 텍스트가 없는 게시글은 `content`가 빈 문자열(`""`)로 반환되며, 이 경우 AWS Translate 자체를 호출하지 않습니다(빈 입력을 굳이 번역 API로 보내지 않아 비용도 아낌).

**실패**: `targetLanguage`가 지원 목록 밖 → 400, 존재하지 않는 `postSno` → 400

### POST `/api/board/comments/{commentSno}/translate` — 댓글 번역 (인증 필요)

게시글 번역과 동일한 방식(캐싱/지원 언어/인증 이유 동일). `data`: `{ "content": "..." }`
