# 회원 탈퇴 + 게시판 신고/차단 설계

> 두 기능이 `member`·`board` 도메인과 인증/조회 경로를 공유해 한 문서로 묶음.

> **진행 상황** (2026-09-09): **Part A · B 모두 구현 완료(로컬 커밋, 미푸시).**
> - Part A 회원 탈퇴 — `DELETE /api/member/me`, `MemberWithdrawService`, 6개 도메인 `PurgeMember*Port`,
>   `crew.LeaveCrewOnWithdrawPort`, login/reissue 가드, `GetMemberNicknamePort` 익명화.
> - Part B 신고 — `board_report` + `post`/`comment.hidden`, `POST /api/board/{posts|comments}/{sno}/reports`,
>   서로 다른 신고자 임계값(`paceleague.board.report.auto-hide-threshold`, 기본 3) 도달 시 자동 숨김.
> - Part B 차단 — `member_block`, `POST/DELETE/GET /api/member/blocks`, `member.GetBlockedMemberSnosPort`,
>   `BoardQueryUseCase.listPosts` 에 viewer 전파, 목록/댓글에서 차단 작성자 제외.
> - 마이그레이션 2개: `2026-09-09_member_withdraw.sql`, `2026-09-09_board_moderation.sql`. 유닛 테스트 4종.
> - **웹 UI 완료**(post.html 글/댓글 신고·차단 버튼 + 신고 사유 `<dialog>`, `web/js/app.js` 헬퍼, `web/js/i18n.js` MODERATION_STRINGS, account-deletion·privacy 문구 갱신). `docs/api.md`·`database.md`·`CLAUDE.md` 갱신.
> - **배포 전 두 마이그레이션을 운영 MySQL 에 먼저 실행.** 아직 미푸시.

## 결정사항 (2026-09-09, 사용자 확인)

| 항목 | 결정 |
|---|---|
| 탈퇴 삭제 방식 | **탈퇴 요청 시 그 자리에서(동기) 소프트삭제.** `member` 행은 남기되 `member_id`에 탈퇴 마커를 넣고 민감정보(닉네임·이메일·비밀번호)를 NULL/공백으로. 러닝·건강 데이터는 같은 요청에서 삭제. **30일 유예·하드삭제 스케줄러 없음**(2026-09-09 사용자 확정 — 초안의 스케줄러 방식 폐기). 복구 불가 |
| 탈퇴 회원 글·댓글 | **익명화 후 유지** — `member_sno`는 그대로 두고 `member` 행 마스킹으로 "탈퇴한 사용자" 표시. 추천수·기록첨부는 끊음 |
| 신고 처리 | **자동 숨김 + 누적** — 서로 다른 신고자 N명(설정값) 도달 시 자동 숨김, `board_report` 테이블에 전부 누적. 관리자 UI 없음(운영자 DB 검토) |
| 차단(block) | 권장안: **게시판 피드 한정 MVP** — 차단 유저의 글은 목록에서 제외, 댓글은 placeholder. 상세(getPost) 직접 링크는 그대로(열린 결정) |

---

# Part A. 회원 탈퇴

## A1. `member` 스키마 변경

```sql
-- docs/migrations/YYYY-MM-DD_member_withdraw.sql
ALTER TABLE member
    ADD COLUMN status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER email,  -- ACTIVE / WITHDRAWN
    ADD COLUMN withdrawn_at DATETIME    NULL                       AFTER status;
CREATE INDEX idx_member_status ON member (status);
```

- `Member` 엔티티: `status`, `withdrawnAt` + `withdraw()`(아래 필드를 한 번에 바꾸는 메서드), `isActive()`.
- 상태는 `ACTIVE` / `WITHDRAWN` **둘뿐** (하드삭제 단계가 없으므로 `PURGED` 불필요).
- `withdrawn_at`은 통계·문의 대응용 기록. 30일 유예에 쓰이지 않음.

## A2. 탈퇴 엔드포인트 — 한 요청에서 전부 처리(동기)

`DELETE /api/member/me` (로그인 필요), body `{ "password": "..." }`

`MemberAuthUseCase.withdraw(memberSno, rawPassword)` — 하나의 트랜잭션:

1. **비밀번호 재확인** (틀리면 400).
2. **크루 가드** (cross-domain): 크루장이면 400 "크루를 먼저 위임하거나 해체하세요". 크루원이면 자동 탈퇴.
   - 신규 shared 포트 `crew.LeaveCrewOnWithdrawPort.onMemberWithdraw(memberSno)` — 크루장이면 예외, 아니면 `CrewMembershipManager` 재사용해 탈퇴 + 잔여 초대/신청 정리.
3. **러닝·건강·게임 데이터 삭제** (아래 A5 표).
4. **`member` 행 마스킹** (DELETE 안 함):

   | 컬럼 | 값 |
   |---|---|
   | `status` | `WITHDRAWN` |
   | `withdrawn_at` | now |
   | `member_id` | `withdrawn_<sno>` (탈퇴 마커 + 원래 아이디를 재가입에 풀어줌. UNIQUE 유지) |
   | `nickname` | `NULL` |
   | `email` | `NULL` |
   | `password_hash` | `''` (빈 문자열 — `matches()`가 절대 통과 못 함) |

5. refresh token: 회원별 인덱스가 없어 즉시 전부 폐기 불가 → `reissue`/`login` status 검사로 차단(A3). access token은 5분 후 만료.
6. 응답 200 "탈퇴가 완료되었습니다."

**복구 불가.** 비밀번호를 즉시 지우므로 되돌릴 수 없다(운영자도). 문의 오면 신규 가입 안내.

> ⚠️ **대용량 계정 주의**: `record_track.points_json`이 LONGTEXT(런 1건 ~9MB)라, 러닝 수백 건인 헤비
> 유저는 이 삭제 트랜잭션이 길어질 수 있다. `DELETE`는 블롭을 로드하지 않아 실제론 빠른 편이지만,
> 안전하게 하려면 `record`/`record_track` 삭제를 배치(예: 500행씩)로 나누는 것을 고려. MVP는 단순
> 동기 삭제로 가되 이 리스크를 인지.

## A3. 인증 경로에서 WITHDRAWN 차단

- `MemberAuthService.login`: `member`를 아이디로 못 찾으면(마스킹된 `member_id`) 자연히 실패. 혹시 남은 세션이 있어도 아래 reissue에서 막힘. 명시적으로 `status != ACTIVE` → 400 "탈퇴 처리된 계정입니다." 도 추가.
- `MemberAuthService.reissue`: `validateAndRevoke`로 memberSno를 얻은 뒤 회원 조회 → `WITHDRAWN`이면 400(토큰은 이미 revoke됨).
- `join`: 탈퇴 시 `member_id`를 `withdrawn_<sno>`로 바꾸므로 **원래 아이디는 즉시 재가입 가능**.
- (선택) `JwtAuthenticationFilter` status 검사 — access token 5분이라 MVP는 생략.

## A4. 탈퇴 회원 표시 익명화

- `MemberQueryService`(`GetMemberNicknamePort` 구현): `status = WITHDRAWN`(또는 `nickname == null`) → `"탈퇴한 사용자"` 반환. **이 한 곳만** 고치면 `board`(작성자/댓글) · `territory`(지도) · `ranking`이 전부 자동 반영.
- `SearchMembersPort`(회원 검색): `status = 'ACTIVE'` 만.
- `GetMemberTierPort`: 티어는 개인정보 아님 → 그대로 유지(또는 SILVER 기본).
- `crew.GetMemberCrewBadgePort`: 탈퇴 시 크루에서 빠지므로 자연히 배지 없음.

## A5. 탈퇴 시 삭제되는 데이터 (A2의 3번, 같은 트랜잭션)

| 대상 | 처리 | 이유 |
|---|---|---|
| `record` / `record_track` (`uno = X`) | **DELETE** | 러닝·건강 데이터(Health Connect 유래). 개인정보처리방침상 "완전 삭제" |
| `score_rank` (`uno = X`) / `member_score` (`member_sno = X`) | **DELETE** | 랭킹에서 제거 |
| `territory` (`owner_member_sno = X`) + 그 `territory_hex` | **DELETE** | 지도에서 그 땅 사라짐 |
| `media` (`member_sno = X`) | 행 DELETE, S3 객체는 best-effort(실패해도 계속) | 업로드 이미지/영상 |
| `crew_member` / `crew_invitation`(inviter·invitee) / `crew_join_request` (member = X) | DELETE | 2번에서 대부분 정리, 잔여분 |
| `member_block` (blocker = X **or** blocked = X) | DELETE | |
| `board_report` (`reporter_member_sno = X`) | DELETE | 단 그 신고로 **이미 숨겨진 글은 숨김 유지** |
| `post` / `comment` | **유지** (`member_sno` 그대로) | member 행 마스킹으로 "탈퇴한 사용자" 표시. 커뮤니티 맥락 보존 |
| `post_vote` / `comment_vote` (`member_sno = X`) | **유지** | 개인식별 정보 아님(회원 sno + 대상 sno). 삭제하면 다른 글 점수 재계산 필요 → 유지. (열린 결정) |
| refresh token(Redis) | 스킵 | 회원별 인덱스 없음, 5분 후 만료 |

- 삭제는 각 도메인이 `application/port/in/shared/PurgeMemberDataPort` 하나씩 노출(`record`/`rank`/`territory`/`media`/`crew`/`board`), `member`의 withdraw 서비스가 전부 호출. `member` 도메인은 다른 테이블을 직접 건드리지 않는다(Clean Arch).
- 전부 한 트랜잭션 → 중간 실패 시 롤백, 사용자는 재시도(멱등: 이미 WITHDRAWN이면 즉시 200).

## A6. 프런트 / 문서

- **앱**: 설정 화면에 "회원 탈퇴" → `DELETE /api/member/me`. 비밀번호 재입력 UI + "복구 불가" 경고.
- **웹**: MVP 범위 밖(웹엔 설정 화면 없음). 후순위.
- `web/ko/account-deletion.html` / `web/en/account-deletion.html`: **이메일 수동 절차 → "앱 내 설정 > 회원 탈퇴" 로 전면 교체. "30일 보관 후 삭제 / 복구 가능" 문구 제거 → "탈퇴 즉시 개인정보 삭제, 복구 불가"**. (Google Play 데이터 삭제 정책상 즉시 삭제는 문제없음 — 오히려 명확)
- `web/*/privacy.html`: 4항 "회원 탈퇴 시 30일간 보관 후 완전 삭제" → **"회원 탈퇴 시 닉네임·이메일·비밀번호 등 개인정보와 러닝·건강 데이터를 즉시 삭제. 아이디는 재사용 가능하도록 치환. 복구 불가"** 로 수정. 3항 건강데이터 "회원 탈퇴 시 완전 삭제" 는 그대로 유효.

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

- **스케줄 잡 없음** — 탈퇴가 그 자리에서 끝나므로 `WithdrawnMemberPurgeScheduler` 불필요(초안 대비 폐기).
  스케줄 잡은 계속 `GpsSessionSweeper` + `DailyTerritoryDigestScheduler` 2개.
- **`BoardQueryUseCase` 시그니처 변경** (viewer 전파) — `BoardController` GET(`listPosts`)에 `@MemberSno(required=false)` 추가, `BoardQueryService`, 관련 테스트.
- **마이그레이션 3개**: `member` status/withdrawn_at, `board_report` + post/comment hidden, `member_block`.
- **cross-domain 포트 추가**: `crew.LeaveCrewOnWithdrawPort`, `member.GetBlockedMemberSnosPort`,
  각 도메인 `port/in/shared/PurgeMemberDataPort`(record/rank/territory/media/crew/board).
- 새 config: `paceleague.board.report.auto-hide-threshold`(기본 3).
- **탈퇴 트랜잭션이 여러 도메인 테이블을 한 번에 지운다** — `member` withdraw 서비스가 6개 도메인의
  `PurgeMemberDataPort`를 순서대로 호출. 한 트랜잭션이라 부분 실패 없음. (대용량 계정 주의 — A2 ⚠️)

# 구현 순서 (권장)

1. **마이그레이션 3개 파일 작성** (운영 적용은 배포 직전, `ddl-auto: validate`).
2. 각 도메인 `PurgeMemberDataPort` + 구현 (`deleteByMemberSno` / `deleteByUno` 쿼리).
3. `crew.LeaveCrewOnWithdrawPort` (크루장 가드 + 크루원 자동 탈퇴).
4. `member`: status 필드 + `DELETE /api/member/me` (비번확인 → 크루가드 → 데이터삭제 → member 마스킹) + login/reissue 가드 + `GetMemberNicknamePort` 익명화.
5. `member_block` 테이블/엔드포인트(`POST/DELETE/GET /api/member/blocks`) + `member.GetBlockedMemberSnosPort`.
6. `BoardQueryUseCase` viewer 전파 + `BoardController` `listPosts` 시그니처 + 차단 필터(목록 제외 / 댓글 placeholder).
7. `board_report` + post/comment `hidden`/`hidden_at` + 신고 엔드포인트 + 자동 숨김(임계값) + 조회 필터.
8. `PostSummaryResponse`/`PostDetailResponse`/`CommentResponse`에 `authorMemberSno`(차단 버튼용).
9. 웹 `post.html` 신고/차단 UI + `web/js/app.js` 헬퍼.
10. `account-deletion.html`(ko/en)·`privacy.html`(ko/en) 문구 갱신 — 30일/복구 문구 제거.
11. 테스트: 탈퇴 시 데이터 삭제·멱등·크루장 거부, 탈퇴 후 login/reissue 거부, 익명화, 자동 숨김 임계값, 차단 필터.
12. `docs/domains.md`·`api.md`·`database.md`·`CLAUDE.md` 갱신.

# 열린 결정사항

- `post_vote`/`comment_vote` (탈퇴자가 누른 것): 유지(권장, 점수 재계산 회피) vs 삭제.
- 신고 자동 숨김 임계값 기본값(3? 5?). 신고자 가중치(티어·계정 나이)까지 볼지.
- 차단이 `getPost` 상세에도 적용되는지(MVP는 피드만).
- 탈퇴한 글/댓글 옆 "탈퇴한 사용자" 문구를 i18n 대상으로 할지(작성자명은 지금도 서버가 내려줌 — `GetMemberNicknamePort`에서 lang 없이 고정 한글 vs 다국어).
- 신고/차단 rate limit (도배 방지) — 후순위.
- 대용량 계정 탈퇴 시 `record`/`record_track` 삭제 배치 분할 여부.
