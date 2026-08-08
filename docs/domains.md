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

`board` 패키지. 보드(카테고리) → 게시글 → 댓글(1단계) → 추천/비추천 구조. 레딧처럼 **조회(GET)는 비로그인도 가능**하고, 작성/삭제/추천(POST/DELETE)만 로그인이 필요합니다 — `SecurityConfig`에 `HttpMethod.GET` 한정으로 `/api/board`, `/api/board/*/posts`, `/api/board/posts/*`, `/api/board/posts/*/comments` 4개 경로만 `permitAll`, 나머지(같은 경로의 POST/DELETE 포함)는 기본 `anyRequest().authenticated()`에 걸림. `BoardController`는 이 4개 조회 엔드포인트에서 `unoOrNull(authentication)`(비로그인이면 `null`)을 쓰고, 나머지 쓰기 엔드포인트는 기존 `uno(authentication)`(비로그인이면 예외)을 그대로 씀 — 인증된 컨트롤러 전체에 반복되는 `uno` 헬퍼 패턴([architecture.md](./architecture.md) 참고)의 변형.

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
- **Redis 캐싱**: 키 `translate:post:{sno}:{lang}` / `translate:comment:{sno}:{lang}`, TTL 180일. 게시글/댓글은 수정 기능이 없어 원문이 불변이므로 같은 조합은 최초 1회만 실제 API를 호출하고 이후는 캐시로 응답 — `RefreshTokenService`(`api/src/main/java/com/example/paceleague/member/service/RefreshTokenService.java`)와 동일한 `StringRedisTemplate` 사용 패턴.
- **번역 소스 언어는 자동 감지**(`SourceLanguageCode: "auto"`) — 게시글/댓글에 작성 언어를 저장하는 컬럼이 없기 때문.
- `Post.content` 최대 길이를 10,000자로 제한(`BoardServiceImpl.CONTENT_MAX_LENGTH`) — 원래는 무제한이었으나, 번역 비용이 글자 수에 비례하므로 이번에 추가.
- **사전 조건**: 운영 EC2의 IAM role(`paceleague-s3-read`)에 `translate:TranslateText` 권한(예: `TranslateReadOnly` 관리형 정책)이 연결되어 있어야 함. 앱은 AWS SDK 기본 자격증명 체인(인스턴스 메타데이터)을 그대로 쓰며, 별도 액세스 키를 설정에 넣지 않음(`common.config.AwsTranslateConfig`). 권한이 없으면 번역 엔드포인트만 500으로 실패하고 나머지 API는 영향받지 않음.
