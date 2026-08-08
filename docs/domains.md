# 도메인별 핵심 비즈니스 로직

## 기록 저장 시 점수 산정 로직

기록 저장(`POST /api/record/save`, `/bulk`)이 성공할 때마다 `RecordServiceImpl.saveRank(...)`가 그 기록 1건에 대한 점수를 계산합니다. 계산 로직은 `RecordController`가 아닌 서비스 계층에 있습니다(`AGENTS.md` 규칙 준수).

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

`rank.enums.RankTier` — 점수 구간별 티어. 각 티어는 `minScore`를 가지며, 점수가 그 이상인 **가장 높은** 티어가 선택됩니다(`RankTierPolicy.calculate`).

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

## 커뮤니티(Board) 도메인

`board` 패키지. 보드(카테고리) → 게시글 → 댓글(1단계) → 추천/비추천 구조. 로그인하지 않으면 조회를 포함해 아무 기능도 쓸 수 없음(`SecurityConfig`에 별도 `permitAll` 없이 기본 `anyRequest().authenticated()`에 그대로 걸림).

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
