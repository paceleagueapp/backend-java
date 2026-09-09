# 회원 탈퇴 + 게시판 신고/차단 설계

> 두 기능이 `member`·`board` 도메인과 인증/조회 경로를 공유해 한 문서로 묶음.
> 상태: **설계만.** 구현은 별도.

## 결정사항 (2026-09-09, 사용자 확인)

| 항목 | 결정 |
|---|---|
| 탈퇴 삭제 방식 | **30일 소프트삭제(status=WITHDRAWN) → 스케줄러가 하드삭제.** 개인정보처리방침·앱스토어 안내와 일치. 스케줄 잡 1개 추가(총 3개) |
| 탈퇴 회원 글·댓글 | **익명화 후 유지** — 작성자 표시를 "탈퇴한 사용자"로, 닉네임 등 개인정보만 제거. 추천수·기록첨부는 끊음 |
| 신고 처리 | **자동 숨김 + 누적** — 서로 다른 신고자 N명(설정값) 도달 시 자동 숨김, `board_report` 테이블에 전부 누적. 관리자 UI 없음(운영자 DB 검토) |
| 차단(block) | 권장안: **게시판 피드 한정 MVP** — 차단 유저의 글은 목록에서 제외, 댓글은 placeholder. 상세(getPost) 직접 링크는 그대로(열린 결정) |

---

# Part A. 회원 탈퇴

## A1. `member` 스키마 변경

```sql
-- docs/migrations/YYYY-MM-DD_member_withdraw.sql
ALTER TABLE member
    ADD COLUMN status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER email,  -- ACTIVE / WITHDRAWN / PURGED
    ADD COLUMN withdrawn_at DATETIME    NULL                       AFTER status;
CREATE INDEX idx_member_status_withdrawn ON member (status, withdrawn_at);
```

- `Member` 엔티티: `status`, `withdrawnAt` 필드 + `withdraw()` / `isActive()` 메서드.
- **PURGED**: 하드삭제 단계에서 개인정보 필드를 마스킹한 상태(행은 남김 — 아래 A5 참조).

## A2. 탈퇴 엔드포인트

`DELETE /api/member/me` (로그인 필요), body `{ "password": "..." }`

`MemberAuthUseCase.withdraw(memberSno, rawPassword)`:
1. 비밀번호 재확인(틀리면 400).
2. **크루 가드** (cross-domain): 크루장이면 400 "크루를 먼저 위임하거나 해체하세요". 크루원이면 자동 탈퇴.
   - 신규 shared 포트 `crew.LeaveCrewOnWithdrawPort.onMemberWithdraw(memberSno)` — 크루장이면 예외, 아니면 `CrewMembershipManager` 재사용해 탈퇴 + 잔여 초대/신청 정리.
3. `member.status = WITHDRAWN`, `withdrawn_at = now`.
4. refresh token: 회원별 인덱스가 없어 즉시 전부 폐기 불가 → `reissue`/`login`에서 status 검사로 차단(아래 A3). access token은 5분 후 만료.
5. 응답: 200 "탈퇴가 완료되었습니다. 30일 내 로그인하시면 복구 문의가 가능합니다."

**복구**: MVP는 운영자가 DB에서 `status='ACTIVE'`, `withdrawn_at=NULL` 로 되돌림(수동). 앱 내 복구 UI는 후순위.

## A3. 인증 경로에서 WITHDRAWN 차단

- `MemberAuthService.login`: 비번 일치 후 `member.status != ACTIVE` → 400 "탈퇴 처리된 계정입니다."
- `MemberAuthService.reissue`: `validateAndRevoke`로 memberSno 얻은 뒤 회원 조회 → WITHDRAWN이면 400(토큰은 이미 revoke됨).
- `join`: `member_id` UNIQUE라 WITHDRAWN 회원의 아이디는 하드삭제(PURGE) 전까지 재사용 불가 — 정상 동작. (PURGE 시 `member_id`를 `deleted_<sno>`로 바꿔 원래 아이디를 풀어줌 — A5)
- (선택) `JwtAuthenticationFilter`에서도 status 검사 — access token 5분이라 없어도 되지만, 확실히 하려면 필터에 member status 캐시 조회 추가. MVP는 생략.

## A4. 탈퇴 회원 표시 익명화

- `MemberQueryService`(`GetMemberNicknamePort` 구현): `status IN (WITHDRAWN, PURGED)` → 닉네임 대신 `"탈퇴한 사용자"` 반환. 이 한 곳만 고치면 `board`(작성자/댓글), `territory`(지도), `ranking` 전부 자동 적용.
- `SearchMembersPort`(회원 검색): `status='ACTIVE'` 만.
- `GetMemberTierPort`: 티어는 개인정보 아님 → 그대로(또는 SILVER 기본). 유지.
- `crew.GetMemberCrewBadgePort`: 탈퇴 시 크루에서 빠지므로 자연히 배지 없음.

## A5. 30일 후 하드삭제 스케줄러

`member.adapter.in.scheduler.WithdrawnMemberPurgeScheduler`
- `@Scheduled(cron = "0 30 3 * * *", zone = "Asia/Seoul")` (매일 새벽 3:30), `@ConditionalOnProperty(name="paceleague.member.purge.enabled", matchIfMissing=true)` — GpsSessionSweeper와 같은 패턴. **단일 인스턴스 전제**(ShedLock 숙제).
- 대상: `status='WITHDRAWN' AND withdrawn_at < now - 30d`, 배치 N명(예: 50).
- 회원 1명당 **별도 트랜잭션**(sweeper처럼 스케줄러에서 구동해 프록시 적용). `member.application.port.in.PurgeWithdrawnMemberUseCase.purge(memberSno)`:

| 대상 | 처리 |
|---|---|
| `post` / `comment` | **삭제 안 함.** `member_sno` 유지 → 아래 member 행 마스킹으로 익명 표시 유지 |
| `post_vote` / `comment_vote` (내가 누른 것) | `member_sno = X` 전부 DELETE (내 추천 이력 = 개인 행동 기록) |
| `record` / `record_track` (`uno = X`) | DELETE (건강 데이터 — 개인정보처리방침상 "완전 삭제") |
| `score_rank` (`uno = X`) / `member_score` (`member_sno = X`) | DELETE (랭킹에서 제거) |
| `territory` (`owner_member_sno = X`) + 그 `territory_hex` | DELETE (지도에서 그 땅 사라짐) |
| `media` (`member_sno = X`) | 행 DELETE. S3 객체 삭제는 best-effort(실패해도 진행) |
| `crew_member` / `crew_invitation`(inviter·invitee) / `crew_join_request` | DELETE (탈퇴 시 대부분 정리됐으나 잔여분) |
| `board_report` (`reporter_member_sno = X`) | DELETE. 단 그 신고로 이미 숨겨진 글은 숨김 유지 |
| `member_block` (blocker=X or blocked=X) | DELETE |
| `member` 행 | **DELETE 하지 않고 마스킹**: `member_id = 'deleted_<sno>'`, `nickname = NULL`, `email = NULL`, `password_hash = ''`, `status = 'PURGED'`. → 참조 무결성 유지, `GetMemberNicknamePort`가 "탈퇴한 사용자" 반환 |
| refresh token(Redis) | 스킵(회원별 인덱스 없음, 이미 만료) |

- 삭제는 각 `*RepositoryPort`에 `deleteByMemberSno(...)` 류 메서드 추가(cross-domain: `record`/`rank`/`territory`/`media`/`crew`/`board`가 각자 노출하는 `PurgeMemberDataPort` 하나로 묶는 것도 방법 — 도메인마다 `port.in.shared.PurgeMemberDataPort` 두고 `member`의 purge 서비스가 전부 호출).
- 실패 시 그 회원은 다음 날 다시 시도(로그 남기고 계속).

## A6. 프런트 / 문서

- **앱**: 설정 화면에 "회원 탈퇴" → `DELETE /api/member/me`. 비밀번호 재입력 UI.
- **웹**: MVP 범위 밖(웹엔 설정 화면 없음). 후순위.
- `web/ko/account-deletion.html` / `web/en/`: 이메일 수동 절차 → "앱 내 설정 > 회원 탈퇴" 안내로 갱신. 30일 정책 유지.
- `web/*/privacy.html`: 이미 30일 명시. "모든 개인정보 완전 삭제"를 "닉네임·이메일·비밀번호 등 개인정보 삭제(아이디는 `deleted_...`로 대체), 건강데이터·러닝기록 완전 삭제"로 미세 조정.

---

# Part B. 게시판 신고 / 차단

## B1. 신고 (Report)

### 테이블 `board_report`

```sql
CREATE TABLE board_report (
    sno                 BIGINT       NOT NULL AUTO_INCREMENT,
    reporter_member_sno BIGINT       NOT NULL,
    target_type         VARCHAR(10)  NOT NULL,            -- POST / COMMENT
    target_sno          BIGINT       NOT NULL,
    reason              VARCHAR(20)  NOT NULL,            -- SPAM / ABUSE / SEXUAL / ETC
    detail              VARCHAR(500) NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'OPEN',  -- OPEN / RESOLVED / DISMISSED
    created_at          DATETIME     NOT NULL,
    PRIMARY KEY (sno),
    UNIQUE KEY uq_report_reporter_target (reporter_member_sno, target_type, target_sno),
    KEY idx_report_target (target_type, target_sno, status)
);
```

### post/comment 숨김 컬럼

```sql
ALTER TABLE post    ADD COLUMN hidden TINYINT(1) NOT NULL DEFAULT 0, ADD COLUMN hidden_at DATETIME NULL;
ALTER TABLE comment ADD COLUMN hidden TINYINT(1) NOT NULL DEFAULT 0, ADD COLUMN hidden_at DATETIME NULL;
```

### 엔드포인트

| 메서드 | 경로 | body |
|---|---|---|
| `POST` | `/api/board/posts/{postSno}/reports` | `{ reason, detail? }` |
| `POST` | `/api/board/comments/{commentSno}/reports` | `{ reason, detail? }` |

- 로그인 필요. 본인 글/댓글 신고 → 400. 이미 신고함 → 멱등(200, 아무 일 안 함).
- `BoardService.reportPost/reportComment`:
  1. 대상 존재 확인, 본인 여부 확인.
  2. `board_report` upsert(UNIQUE 충돌 시 무시).
  3. `select count(distinct reporter_member_sno) from board_report where target=X and status='OPEN'` ≥ `paceleague.board.report.auto-hide-threshold`(기본 3) → 대상 `hidden=1, hidden_at=now`.

### 숨김 효과 (`BoardQueryService`)

- `listPosts`: `hidden=0` 만 (repo 쿼리 조건 추가).
- `getPost`: `hidden=1` 이면 404 `IllegalArgumentException`(GlobalExceptionHandler가 400… → 404 원하면 `NoResourceFoundException` 계열 필요. MVP는 400 "삭제되었거나 숨겨진 게시글입니다"). **작성자 본인도 못 봄**(단순화).
- `listComments`: `hidden=1` 댓글은 스레드 구조 유지를 위해 **placeholder**로 — `content="신고 누적으로 숨겨진 댓글입니다"`, `nickname` 비움, 투표/답글 버튼 없음. 대댓글은 그대로 노출.
- 썸네일/스니펫(`PostSummaryResponse`)도 hidden 글은 애초에 목록에서 빠지므로 해당 없음.

### 복구

운영자가 DB에서 `post/comment.hidden=0`, 해당 `board_report.status='DISMISSED'`. 문서에 명시. (임계값 낮으면 소수 악의적 신고로 정상 글이 숨겨질 수 있음 — 임계값 튜닝 + 운영 모니터링 쿼리 제공.)

### 신고 사유 i18n

enum 코드만 API로 주고받고, 라벨은 클라이언트. `web/js/i18n.js`에 `reportReasonSpam` 등 추가. 앱도 자체 라벨.

## B2. 차단 (Block)

### 테이블 `member_block`

```sql
CREATE TABLE member_block (
    sno                BIGINT   NOT NULL AUTO_INCREMENT,
    blocker_member_sno BIGINT   NOT NULL,
    blocked_member_sno BIGINT   NOT NULL,
    created_at         DATETIME NOT NULL,
    PRIMARY KEY (sno),
    UNIQUE KEY uq_block (blocker_member_sno, blocked_member_sno),
    KEY idx_block_blocker (blocker_member_sno)
);
```

### 엔드포인트 (`member` 도메인)

| 메서드 | 경로 | 설명 |
|---|---|---|
| `POST` | `/api/member/blocks` | body `{ blockedMemberSno }`. 자기 자신 → 400. 멱등 |
| `DELETE` | `/api/member/blocks/{blockedMemberSno}` | 차단 해제 |
| `GET` | `/api/member/blocks` | 내 차단 목록(닉네임 포함) |

### 차단 필터링

- **작성자 memberSno 노출 필요**: `PostSummaryResponse` / `PostDetailResponse` / `CommentResponse`에 `authorMemberSno` 추가(차단 버튼용). memberSno는 이미 회원 검색 API로 노출되므로 프라이버시 영향 미미.
- cross-domain 포트 `member.GetBlockedMemberSnosPort.getBlockedBy(viewerMemberSno) → Set<Long>` (board→member, 기존 `GetMemberNicknamePort`와 같은 방향).
- `BoardQueryUseCase.listPosts` 시그니처에 **viewer memberSno 추가** — `BoardController.listPosts`에 `@MemberSno(required = false)` 파라미터 추가(현재 없음). `getPost`/`listComments`는 이미 있음.
- `BoardQueryService`:
  - `listPosts`: 차단 목록 조회 → `member_sno NOT IN (:blocked)` (비면 조건 스킵). Spring Data 파생 쿼리로는 어려우니 `@Query`로.
  - `listComments`: 차단 유저 댓글 → placeholder("차단한 사용자의 댓글입니다"), 대댓글 유지.
  - `getPost`: MVP는 **필터 안 함**(직접 링크로 온 상세는 그대로). 열린 결정.
- 차단은 **단방향** — A가 B를 차단해도 B에게는 A 글이 그대로 보임(표준 동작).
- 차단 유저를 크루 초대에서도 빼는 등 확장은 phase 2.

## B3. 웹 UI

- `web/post.html`: 글/댓글 액션에 "신고"·"차단" 추가(로그인 필요 → `redirectToLoginIfNeeded`). 신고는 사유 선택 다이얼로그(`<dialog>`).
- `web/index.html` 피드: 서버가 이미 필터링하므로 클라 변경 최소(hidden/blocked 글이 응답에 안 옴).
- `web/js/app.js`: `reportPost/reportComment/blockMember/unblockMember` 헬퍼.

---

# 공통 인프라 영향

- **스케줄 잡 3개째** (`WithdrawnMemberPurgeScheduler`) — `CLAUDE.md`·`SchedulingConfig` 문구 갱신. 셋 다 단일 인스턴스 전제 → ShedLock 숙제 누적.
- **`BoardQueryUseCase` 시그니처 변경** (viewer 전파) — `BoardController` GET 4개, `BoardQueryService`, 관련 테스트.
- **마이그레이션 3개**: `member` status/withdrawn_at, `board_report` + post/comment hidden, `member_block`.
- **cross-domain 포트 추가**: `crew.LeaveCrewOnWithdrawPort`, `member.GetBlockedMemberSnosPort`, 각 도메인 `PurgeMemberDataPort`(record/rank/territory/media/crew/board).
- 새 config: `paceleague.member.purge.enabled`(기본 true) / `.retention-days`(30), `paceleague.board.report.auto-hide-threshold`(3).

# 구현 순서 (권장)

1. **마이그레이션 3개 파일 작성** (운영 적용은 배포 직전, `ddl-auto: validate`).
2. `member`: status 필드 + `withdraw` 엔드포인트 + login/reissue 가드 + `GetMemberNicknamePort` 익명화. (탈퇴는 되지만 데이터는 아직 남음)
3. 각 도메인 `PurgeMemberDataPort` + `WithdrawnMemberPurgeScheduler`. (30일 하드삭제)
4. `member_block` 테이블/엔드포인트 + `GetBlockedMemberSnosPort`.
5. `BoardQueryUseCase` viewer 전파 + `BoardController` 시그니처 + 차단 필터.
6. `board_report` + post/comment `hidden` + 신고 엔드포인트 + 자동 숨김 + 조회 필터.
7. `PostSummaryResponse`/`CommentResponse`에 `authorMemberSno`.
8. 웹 `post.html` 신고/차단 UI + `app.js` 헬퍼.
9. `account-deletion.html`·`privacy.html` 문구 갱신.
10. 테스트: purge 순서/멱등, 자동 숨김 임계값, 차단 필터, 탈퇴 후 login 거부, 익명화.
11. `docs/domains.md`·`api.md`·`database.md`·`CLAUDE.md` 갱신.

# 열린 결정사항

- 하드삭제 시 `member` 행: 마스킹(status=PURGED, 권장) vs 완전 DELETE(post.member_sno가 dangling → getNickname null → "알 수 없음").
- 신고 자동 숨김 임계값 기본값(3? 5?). 신고자 가중치(티어·계정 나이)까지 볼지.
- 차단이 `getPost` 상세에도 적용되는지.
- 복구 플로우 — 수동(MVP) vs 앱 UI + 자동 복구(재로그인 시).
- `record`/`territory` 하드삭제(권장, 건강데이터) vs 익명 보존.
- 신고/차단 rate limit (도배 방지).
