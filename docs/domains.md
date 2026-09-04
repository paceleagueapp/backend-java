# 도메인별 핵심 비즈니스 로직

## 기록 저장 시 점수 산정 로직

기록 저장(`POST /api/record/save`, `/bulk`, 그리고 GPS 세션 저장 `POST /api/record/gps` — 이것도 내부적으로 `RecordService.create`를 재사용)이 성공할 때마다 `RecordServiceImpl.saveRank(...)`가 그 기록 1건에 대한 점수를 계산합니다. 계산 로직은 `RecordController`가 아닌 서비스 계층에 있습니다(`AGENTS.md` 규칙 준수).

### 1. 기본 점수 (base score)

```text
distanceKm = distanceRecord(미터) / 1000
baseScore  = round(distanceKm * 10)   // 1km당 10점, 소수점 반올림(HALF_UP)
```

### 2. 페이스 보너스 (scaled score)

```text
paceSecondsPerKm = round(durationSeconds / distanceKm)   // durationSeconds = endTime - startTime

pace <= 330초(5:30/km 이하)  → scaledScore = round(baseScore * 0.2)   // +20%
pace <= 390초(6:30/km 이하)  → scaledScore = round(baseScore * 0.1)   // +10%
그 외                        → scaledScore = 0
```

### 3. 주간 횟수 보너스 (add score)

기록의 `startTime`이 속한 **주(월요일 00:00 ~ 다음 월요일 00:00 직전)** 동안 저장된 기록 수(`weeklyRunCount`, 이번 기록 포함)를 기준으로:

```text
weeklyRunCount >= 5 → addScore = 120
weeklyRunCount >= 3 → addScore = 50
그 외                → addScore = 0
```

### 4. 합산 및 저장

```text
totalScore = baseScore + scaledScore + addScore
```

- 이 `totalScore`는 `score_rank` 테이블(`Rank` 엔티티)에 **개별 기록에 대한 점수 로그**로 1건 저장됩니다 (`score`, `scaledScore`, `addScore`를 각각 컬럼에 보관).
- 동시에 시즌별 누적 테이블인 `member_score`(`MemberScore` 엔티티)를 조회해(비관적 락 `PESSIMISTIC_WRITE`로 동시성 보호) `totalScore`만큼 더합니다. 해당 시즌에 아직 레코드가 없으면 **1500점**을 초기값으로 새로 생성 후 더합니다.
- `MemberScore.addScore(...)`가 호출될 때마다 `RankTierPolicy.calculate(totalScore)`로 `tier`도 함께 재계산되어 저장됩니다 (조회 시점이 아니라 **점수 변경 시점**에 티어가 갱신됨).

## 티어 (Rank Tier)

`rank.domain.enums.RankTier` — 점수 구간별 티어. 각 티어는 `minScore`를 가지며, 점수가 그 이상인 **가장 높은** 티어가 선택됩니다(`rank.domain.policy.RankTierPolicy.calculate`).

| 티어 | 최소 점수 |
|---|---|
| BRONZE | 0 |
| SILVER | 1,500 |
| GOLD | 3,000 |
| PLATINUM | 5,000 |
| DIAMOND | 8,000 |
| MASTER | 12,000 |
| CHALLENGER | 20,000 |

- 신규 회원의 시즌 초기 점수가 1500점이므로, 사실상 모든 회원은 **SILVER에서 시작**합니다 (BRONZE는 도달할 일이 이론상 없음, 최초 점수 미달 상황이 없다면).
- `RankTier.next()`는 `values()` 배열의 다음 인덱스를 반환하며, `CHALLENGER`에서는 `null` (최고 티어).

## `rank` vs `ranking` 조회 로직 차이

### 내 랭크 조회 (`RankQueryService.getMyRank`)

- 현재 시즌(`SeasonRepository.findTopByOrderByStartDtDesc()` — `start_dt` 기준 최신 시즌)에 대한 내 `MemberScore`를 조회.
- 없으면 기본값(`totalScore=1500`, `tier=SILVER`)으로 응답 (DB에 레코드를 새로 만들지는 않음, 순수 조회 응답값일 뿐).
- `remainingScore = nextTier.minScore - totalScore` (다음 티어까지 남은 점수). 최고 티어면 0.

### 랭킹/리더보드 조회 로직

`RankingQueryService.getRankingPage`, 순위 산정 방식:

1. **Top3**: `RankingRepository.findTop3` — 시즌 내 `total_score desc, update_at asc, member_sno asc` 순으로 상위 3명 (네이티브 SQL, `member_score` ⋈ `member`).
2. **내 순위(myRank)** 계산:
   - 내 `MemberScore`가 있으면 `countHigherRankers` 쿼리로 "나보다 순위가 높은 사람 수 + 1".
     - 동점자 처리: `total_score`가 같으면 `update_at`이 더 이른 사람이 더 높은 순위, 그것도 같으면 `member_sno`가 더 작은 사람이 더 높은 순위 (Top3 정렬 기준과 동일한 tie-break).
   - 내 `MemberScore`가 없으면(이번 시즌 미기록) "점수가 1500 초과인 사람 수 + 1"을 기본 순위로 사용.
3. **내 주변 5명(aroundRanks)**: `offset = max(0, myRank - 3)`, `limit = 5`로 같은 정렬 기준의 목록을 조회. 즉 내가 1~3위여도 항상 1위부터 보여주고(offset 0으로 클램프), 그 외에는 내 순위 기준 위로 2명 정도 포함되도록 구성.
4. 두 목록 모두 `RankingUserResponse`로 변환하며 각 항목의 `rank`는 조회 시작 순번(`startRank`)에 배열 인덱스를 더해 부여 (DB에 순위 컬럼이 저장되어 있는 게 아니라 조회 시점에 계산됨).
5. `me` 플래그는 `ranking.getMemberSno().equals(myMemberSno)`로 판정. `myMemberSno`가 `null`이면(비로그인 컨텍스트) `Long.equals(null)`이 항상 `false`를 반환하므로 자연스럽게 모든 항목이 `me: false`가 됨 — `getTop10()`이 바로 이 방식으로 로그인 없이 재사용.

`RankingQueryService.getTop10()`은 `getRankingPage`와 별도 쿼리를 만들지 않고, `aroundRanks`가 쓰는 `findAroundRanking(seasonSno, limit, offset)`을 `limit=10, offset=0`으로 그대로 재사용합니다. `GET /api/ranking/top10`(공개, 인증 불필요)으로 노출되며 `paceleague.co.kr` 랜딩 페이지가 이 응답을 표시합니다 — 자세한 내용은 [api.md](./api.md#get-apirankingtop10--상위-10명-랭킹-조회-공개-인증-불필요) 참고.

## 앱 버전 체크 로직

`AppVersionService.checkVersion(platform, currentVersion)`:

- `app_version_policy` 테이블에서 플랫폼(`ANDROID`/`IOS`)별 정책 1건을 조회 (`platform`은 유니크 가정, 없으면 400 에러).
- 버전 비교는 `.`으로 분리한 각 세그먼트를 정수로 비교(세그먼트 개수가 다르면 짧은 쪽을 0으로 채움) — 즉 `"1.4"`와 `"1.4.0"`은 동일하게 취급됨.
- 판정:
  - `currentVersion < minRequiredVersion` → `FORCE` (강제 업데이트, `forceUpdate=true`)
  - `minRequiredVersion <= currentVersion < latestVersion` → `OPTIONAL` (선택 업데이트)
  - `currentVersion >= latestVersion` → `NONE`
- `maintenance`는 `AppVersionPolicy.maintenanceYn`이 `"Y"`(대소문자 무관)일 때 `true`.
- 이 로직은 업데이트 여부와 점검 여부를 **독립적으로** 계산하므로, 이론상 "점검 중이면서 강제 업데이트도 필요"한 응답도 나올 수 있음 (클라이언트가 두 값을 모두 확인해야 함).

## 시즌 (Season)

- `season` 테이블은 시즌 번호(`season`)와 시작/종료 시각(`start_dt`, `end_dt`)만 가짐.
- "현재 시즌"은 별도의 활성 플래그가 아니라 **`start_dt` 기준 가장 최근 시즌**으로 판정됩니다 (`SeasonRepository.findTopByOrderByStartDtDesc()`).
  - 즉 미래에 시작하는 시즌 데이터를 미리 넣어두면, `end_dt`가 아직 안 지났어도 그 시즌이 "현재 시즌"으로 잡힐 수 있으니 시즌 데이터 입력 순서/시점에 주의가 필요함.
- 기록 저장 시 그 시점의 "현재 시즌"이 `Record.season` 및 `MemberScore.seasonSno`에 스냅샷처럼 기록됨.

## 러닝 땅따먹기(Territory) 도메인

`territory` 패키지 (2026-08-27 1차 구현). 러닝 GPS 경로가 이룬 닫힌 도형을 "땅"으로 저장하고, 겹치는 러닝으로 데미지를 주고받으며 HP가 0이 되면 소유권이 넘어가는 게임 기능. 전체 기획은 Notion "러닝 땅따먹기" 문서 참고 — 이번 범위는 그중 1차 슬라이스이며, H3/S2 셀 시스템·시즌 리셋·크루전·부정기록 판정 정교화는 제외.

### 땅따먹기 모드

일반 러닝은 땅 판정 대상이 아니다. 앱이 러닝 시작 시 `POST /api/record/gps` 첫 청크에 `territoryMode: true`를 실어야 그 세션이 `record_track.territory_mode = 1`로 생성되고(이후 불변), 러닝 종료(`finished: true` 또는 `GpsSessionSweeper` 자동 마감) 시점에만 땅 판정이 돈다. `SaveGpsSessionServiceImpl.finalizeRun` → `ProcessTerritoryRunUseCase.process` 호출. 이 호출은 **best-effort** — `ProcessTerritoryRunServiceImpl`이 `@Transactional(REQUIRES_NEW)`이고 호출부가 예외를 잡아 삼키므로, 땅 판정이 실패해도 러닝 기록/점수는 그대로 저장된다(`record→rank`의 `ApplyScoreUseCase`가 호출자 트랜잭션에 합류해 실패 시 함께 롤백되는 것과 대비되는 의도적 차이).

### 땅 판정 규칙 (`ProcessTerritoryRunServiceImpl`)

1. **닫힌 도형 판정** (`ClosedLoopDetector`): 경로의 시작·끝 좌표가 `paceleague.territory.close-threshold-meters`(기본 50m) 이내면 닫힘. 아니면 no-op(`NO_LOOP`).
2. **도형 유효성** (`PolygonGeometry` + `TerritoryClaimValidator`): 둘레 ≥ `min-perimeter-meters`(300), 면적은 `min-area-sqm`(10,000) ~ `max-area-sqm`(5,000,000) 범위. 벗어나면 no-op(`INVALID_SHAPE`).
3. **겹침 조회**: 러닝 bbox와 겹치는 `ACTIVE` territory를 비관적 락(`PESSIMISTIC_WRITE`)으로 조회. 실제 폴리곤 교집합 면적(`PolygonGeometry.intersectionAreaSqm`, JTS)이 0 초과인 것만 대상.
   - **내 소유 땅** → `heal`: 회복량 = `겹친면적/내땅면적 × maxHp × heal-factor`(0.5), 최대치까지.
   - **남의 땅** → `damage`: 데미지 = `겹친면적/대상땅면적 × maxHp × attack-factor`(0.5), 최소 1. `territory_contribution`에 기여도 1건 기록.
     - HP가 0 이하가 되면 최근 1시간(`contribution-window-minutes`) 기여도 합이 가장 큰 사람이 **즉시 점령**(동점 → 가장 최근 기여자 → 이번 공격자). 소유자 변경 + HP를 maxHp로 리셋 + 해당 땅의 `territory_contribution` 전체 삭제(`TerritoryDamagePolicy.resolveNewOwner`).
4. **겹친 땅이 하나도 없으면** → 새 `Territory` 생성(HP = `default-max-hp` 100, `season` = 생성 시점 시즌 번호 스냅샷). 겹친 게 있었으면 새 땅은 만들지 않는다("빈 땅이면 즉시 내 땅").

### 지도 조회 (`GET /api/territory/map`)

공개(인증 불필요). 지도 bounds(남서/북동 위경도) + 줌 레벨로 `ACTIVE` territory를 bbox 겹침으로 조회. 줌이 `paceleague.territory.min-zoom`(13) 미만이면 빈 목록 + `zoomTooLow: true`(데이터 과다 방지). 최대 `map-max-results`(300)개, 면적 큰 순. 소유자 닉네임은 `member.GetMemberNicknamePort`, 티어는 `rank.GetMemberTierPort`(+ `RankTierLabelPolicy` 라벨) — 소유자별 캐시로 N+1 회피. 로그인 상태면 각 항목의 `mine` 플래그가 채워진다. `web/territory.html`(Google Maps JS API)이 이 응답을 폴리곤으로 그린다.

### 면적 랭킹 (`GET /api/territory/ranking`)

공개(인증 불필요). `owner_member_sno`별 `SUM(area_sqm)`(ACTIVE만) 내림차순 랭킹. 네이티브 집계 쿼리(`TerritoryJpaRepository.findTopOwnersByArea`) → `TerritoryOwnerArea` → `TerritoryQueryServiceImpl.getRanking`이 소유자별 닉네임/티어를 붙여 `TerritoryRankingResponse`로 반환. 최대 `ranking-max-results`(100)명. 로그인 상태면 본인 항목에 `mine: true`. `web/territory.html` 지도 우상단 "랭킹" 패널이 사용. 지도 조회와 같은 서비스(`TerritoryQueryServiceImpl`)가 `GetTerritoryRankingUseCase`도 구현한다.

### 설정 (`paceleague.territory.*`, `TerritoryProperties`)

`app.jwt`(`JwtProperties`)와 같은 `@ConfigurationProperties` 방식. `application*.yml`에는 없고 아래 기본값을 사용: `min-zoom`(13), `map-max-results`(300), `ranking-max-results`(100), `close-threshold-meters`(50), `min-perimeter-meters`(300), `min-area-sqm`(10000), `max-area-sqm`(5000000), `default-max-hp`(100), `attack-factor`(0.5), `heal-factor`(0.5), `contribution-window-minutes`(60).

### 알려진 한계 (v1)

- 폴리곤 면적/교집합은 공간 DB 타입 대신 등거리 근사 평면 투영 + JTS로 Java에서 계산(`record_track.points_json`과 동일한 "좌표는 JSON, 계산은 Java" 방침). 수 km 규모에서 오차 무시 수준.
- `REQUIRES_NEW` 특성상, 땅 판정 성공 후 바깥 GPS 트랜잭션이 실패하면 러닝은 롤백되어도 땅은 남는다(확률 낮음 — 바깥 트랜잭션의 마지막 단계가 세션 저장뿐).
- 모바일 앱이 `territoryMode`를 보내기 전까지는 실제로 아무 땅도 생성되지 않는다(하위호환: 필드 없으면 false).

## 크루(Crew) 도메인

`crew` 패키지 (2026-08-28 1·2단계 구현). 게임의 길드. 전체 기획은 Notion "크루" 문서, 단계별 작업은 [crew-implementation-plan.md](./crew-implementation-plan.md) 참고. 1단계: 생성·검색·초대·가입신청·크루원 관리. 2단계: 크루원 랭킹 + 게시판/랭킹 크루 배지. 땅따먹기 크루전은 3단계.

### 핵심 규칙

- **한 회원 = 한 크루**: `crew_member.member_sno` 전역 UNIQUE로 강제. 새 크루를 만들거나 다른 크루에 들어가려면 먼저 탈퇴해야 한다.
- **가입 방식은 승인제 하나만**(v1). `join_policy` 컬럼은 두되 항상 `APPROVAL`.
- **초대 권한은 크루장만**. 부크루장/운영진 역할 없음.
- **알림 시스템 없음** — 초대받은 회원은 `GET /api/crew/invitations/me` 를 봐야 안다.
- **크루장 탈퇴 불가** — 크루장 위임(`POST /api/crew/{sno}/leader`) 또는 크루 해체 후에만.
- **크루 해체는 크루장 혼자 남았을 때만**. 해체 시 `crew`/`crew_member`/`crew_invitation`/`crew_join_request` 전부 **하드 삭제**(JPA cascade 안 씀 — 서비스에서 순서 명시). 크루명은 즉시 재사용 가능.

### 가입 확정 동시성 (`CrewMembershipManager`)

초대 수락 / 가입신청 승인 양쪽이 같은 "가입 확정" 로직을 쓴다: `Crew` row를 `PESSIMISTIC_WRITE`로 잠근 뒤 정원(`isFull`)·중복 소속을 재확인하고 `CrewMember` insert + `member_count++`. `MemberScore.addScore`와 동일한 락 패턴. 마지막 한 자리에 두 명이 동시에 들어오는 경쟁을 직렬화한다. 단일 인스턴스 전제.

### 크루 상세 응답 (`GET /api/crew/{sno}`)

로그인 필요. 요청자가 그 크루의 크루원이면 `notice` + `members`(닉네임·티어 배지·크루장 표시)가 함께 오고, 아니면 공개 정보(이름·아이콘·소개·인원)만 온다(`viewerIsMember`/`viewerIsLeader` 플래그). 크루 검색(`GET /api/crew/search`)은 비로그인 공개, 공개 정보만.

### 크루 아이콘

`iconMediaId`(미리 `media` 업로드로 APPROVED된 이미지의 sno)를 받아 `media.GetApprovedMediaUrlPort.requireApprovedUrl(mediaSno, ownerMemberSno)`로 본인 소유 + APPROVED 검증 후 그 URL을 `crew.icon_url`에 복사 저장. 아이콘 미설정 크루는 클라이언트에서 크루명 첫 글자 + 해시 색상 플레이스홀더로 표시.

### 회원 검색 (`GET /api/member/search?q=`)

크루 초대 대상을 고르기 위한 엔드포인트(로그인 필요). `member` 도메인에 추가된 `SearchMembersPort` — `member_id` 접두 일치 또는 `nickname` 부분 일치, 아이디 정확 일치 우선.

### 크루원 랭킹 (`GET /api/crew/{sno}/ranking`) — 2단계

크루원만 조회. 크루 내 회원들을 **현재 시즌 누적 점수**(기존 `MemberScore`) 기준 내림차순. `rank`에 추가된 `GetMemberSeasonScoresPort`로 크루원 전체 점수를 한 번에 배치 조회하고, 점수 없는 회원은 기본값(1500/SILVER)으로 채운다. `rank`가 `season.getSeason()`(번호)로 조회하는 기존 방식을 그대로 씀([rank vs ranking](#rank-vs-ranking-조회-로직-차이) 참고).

### 게시판·랭킹 크루 배지 (§9) — 2단계

크루 소속 회원은 게시판 글 목록/상세, 랭킹 위젯에서 닉네임·티어 옆에 **크루명 + 크루 아이콘**이 함께 표시된다. `crew`가 노출하는 `GetMemberCrewBadgePort`(`getBadge(memberSno)` 단건 / `getBadges(memberSnos)` 배치)를 `board`·`ranking`이 소비. `PostSummaryResponse`/`PostDetailResponse`에 `authorCrewName`/`authorCrewIconUrl`, `RankingUserResponse`에 `crewName`/`crewIconUrl` 추가(크루 없으면 null). 조회 시점 계산이라 탈퇴/해체 시 자연히 사라짐(캐시만 주의). `web/index.html`(글 목록·TOP10 위젯)·`web/post.html`(글 상세)에 렌더링.

## 커뮤니티(Board) 도메인

`board` 패키지. 보드(카테고리) → 게시글 → 댓글(1단계) → 추천/비추천 구조. 레딧처럼 **조회(GET)는 비로그인도 가능**하고, 작성/삭제/추천(POST/DELETE)만 로그인이 필요합니다 — `SecurityConfig`에 `HttpMethod.GET` 한정으로 `/api/board`, `/api/board/*/posts`, `/api/board/posts/*`, `/api/board/posts/*/comments` 4개 경로만 `permitAll`, 나머지(같은 경로의 POST/DELETE 포함)는 기본 `anyRequest().authenticated()`에 걸림. `BoardController`는 이 4개 조회 엔드포인트에서 `@MemberSno(required = false) Long memberSno`(비로그인이면 `null`)를 쓰고, 나머지 쓰기 엔드포인트는 기본값인 `@MemberSno Long memberSno`(비로그인이면 예외)를 그대로 씀 — 인증된 컨트롤러 전체가 공유하는 `common.web.MemberSnoArgumentResolver`([architecture.md](./architecture.md) 참고)의 `required` 옵션 차이.

### 시각(createAt/updateAt)은 전세계 사용자를 가정해 UTC로 저장 — 다른 도메인과 다른 규칙

`record`/`rank` 등 기존 도메인은 `LocalDateTime.now()`(서버 시스템 기본 타임존 기준)를 그대로 쓰지만, `board`는 전세계에서 접속하는 걸 가정한 기능이라 의도적으로 `LocalDateTime.now(ZoneOffset.UTC)`를 써서 **항상 UTC 값**을 저장합니다(컬럼 타입은 여전히 `LocalDateTime`이라 값에 타임존 표시가 붙지는 않지만, 값 자체가 UTC로 고정됨). 프론트엔드(`web/js/app.js`의 `formatLocalTime`)가 이 문자열을 UTC로 간주해 `Z`를 붙여 파싱한 뒤, 보는 사람 브라우저의 로케일/타임존으로 자동 변환해서 표시합니다 — 즉 등록 시각은 서버에 UTC로 한 번만 저장되고, "어느 나라 시간으로 보여줄지"는 전적으로 클라이언트에서 결정됩니다.

### 추천/비추천 토글 규칙 (`BoardServiceImpl.applyPostVote`/`applyCommentVote`)

같은 대상(게시글 또는 댓글)에 대한 내 투표 여부에 따라 동작이 갈림:

| 현재 내 투표 | 요청 값 | 결과 |
|---|---|---|
| 없음 | `1` 또는 `-1` | 신규 투표 생성, `score`에 그 값만큼 증감 |
| `1` | `1` (동일) | 투표 취소(행 삭제), `score`에서 `-1` |
| `-1` | `-1` (동일) | 투표 취소(행 삭제), `score`에서 `+1` |
| `1` | `-1` (반대) | 투표 전환, `score`에서 `-2`(취소분 -1 + 반대표 -1) |
| `-1` | `1` (반대) | 투표 전환, `score`에서 `+2` |

`post`/`comment`의 `score` 컬럼은 투표 시점에 `PESSIMISTIC_WRITE` 락으로 해당 row를 잠근 뒤 증감(`MemberScore.addScore`와 동일한 패턴). `post_vote`/`comment_vote`에는 `(member_sno, post_sno)`/`(member_sno, comment_sno)` DB 유니크 제약이 실제로 걸려 있어 중복 투표를 원천 차단함([database.md](./database.md) 참고).

### 댓글 1단계 중첩 규칙

`comment.parent_comment_sno`가 `NULL`이면 최상위 댓글, 값이 있으면 답글. 답글 작성 요청 시 `parentCommentSno`가 가리키는 댓글이 **이미 답글이면(`parentCommentSno`가 있으면) 400으로 거부** — 즉 답글에는 답글을 달 수 없고 딱 1단계까지만 허용. 조회(`GET .../comments`)는 페이징 없이 게시글의 전체 댓글을 가져와 최상위/답글로 그룹핑해 반환.

### 삭제 시 연쇄 삭제 규칙 (하드 삭제, 복구 불가)

- 게시글 삭제 → 그 게시글의 모든 댓글(최상위+답글) + 모든 댓글의 추천 기록 + 게시글 자신의 추천 기록이 함께 삭제됨.
- 댓글 삭제 → 그 댓글의 답글들 + 답글들의 추천 기록 + 그 댓글 자신의 추천 기록이 함께 삭제됨.
- JPA cascade 애노테이션을 쓰지 않고 `BoardServiceImpl`이 삭제 순서(투표 → 하위 댓글 → 본체)를 명시적으로 제어함.
- 소프트 삭제/감사 로그 없음 — 모더레이션이 필요해지면 별도 작업.

### 조회수

`GET /api/board/posts/{sno}` 호출마다 원자적 `UPDATE post SET view_count = view_count + 1`로 증가. 중복 방지(동일 사용자 재방문 시 미증가 등) 없음 — 의도된 단순화.

### 정렬 (`top`)

`GET /api/board/{boardSno}/posts?sort=top`은 `score DESC, create_at DESC` 순으로 정렬됨(레딧의 "hot" 알고리즘 같은 시간 가중치는 없음, 순수 추천수 내림차순).

### 게시글/댓글 번역 (`TranslationService`)

`POST /api/board/posts/{postSno}/translate`, `POST /api/board/comments/{commentSno}/translate` — AWS Translate(`software.amazon.awssdk:translate`)로 제목/본문을 대상 언어로 번역해 반환. **원문은 절대 수정/대체되지 않음** — 번역은 매 요청마다 별도로 계산되는 부가 뷰이고, `Post`/`Comment` 엔티티의 `title`/`content`는 그대로 유지된다. `web/post.html`(게시글 본문+댓글)과 `web/index.html`(목록의 제목만, `PostTranslationResponse.title`만 사용)이 모두 이를 "번역 보기"를 누르면 화면에서만 원문↔번역을 토글하는 방식으로 구현(서버에 원문을 다시 보내지 않고, 클라이언트가 최초 응답을 기억했다가 토글).

- **지원 언어**: `ko`/`en`/`ja`/`zh`/`es`/`fr`/`de`/`pt`/`vi`/`th` 10개 — `TranslationServiceImpl.SUPPORTED_LANGUAGES`와 `web/js/i18n.js`의 `SUPPORTED_LANGUAGES`가 반드시 일치해야 함(하나를 바꾸면 다른 쪽도 같이 바꿀 것).
- **비용 통제를 위해 조회성 동작인데도 로그인 필요**: board의 다른 조회(GET)는 전부 비로그인 공개인데, 번역만 유일하게 외부 유료 API(AWS Translate)를 호출하는 조회라 비로그인 남용으로 비용이 새는 걸 막기 위한 예외.
- **Redis 캐싱**: 키 `translate:post:{sno}:{lang}` / `translate:comment:{sno}:{lang}`, TTL 180일. 게시글/댓글은 수정 기능이 없어 원문이 불변이므로 같은 조합은 최초 1회만 실제 API를 호출하고 이후는 캐시로 응답 — `member.adapter.out.token.RedisRefreshTokenAdapter`(`api/src/main/java/com/paceleague/member/adapter/out/token/RedisRefreshTokenAdapter.java`)와 동일한 `StringRedisTemplate` 사용 패턴.
- **번역 소스 언어는 자동 감지**(`SourceLanguageCode: "auto"`) — 게시글/댓글에 작성 언어를 저장하는 컬럼이 없기 때문.
- `Post.content` 최대 길이를 10,000자로 제한(`BoardServiceImpl.CONTENT_MAX_LENGTH`) — 원래는 무제한이었으나, 번역 비용이 글자 수에 비례하므로 이번에 추가.
- **사전 조건**: 운영 EC2의 IAM role(`paceleague-s3-read`)에 `translate:TranslateText` 권한(예: `TranslateReadOnly` 관리형 정책)이 연결되어 있어야 함. 앱은 AWS SDK 기본 자격증명 체인(인스턴스 메타데이터)을 그대로 쓰며, 별도 액세스 키를 설정에 넣지 않음(`common.config.AwsTranslateConfig`). 권한이 없으면 번역 엔드포인트만 500으로 실패하고 나머지 API는 영향받지 않음.
