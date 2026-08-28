# 크루(Crew) 구현 계획

Notion "크루" 문서(기획) + 현재 코드베이스 컨벤션 기준 구현 리스트.
전체 기획: Notion `크루` 문서. 단계 구분은 그 문서 §14를 따름.

> **진행 상황** (2026-08-28):
> - **1단계 완료** — `crew` 도메인, `GET /api/member/search`, `media.GetApprovedMediaUrlPort`, `web/crew.html`+`web/js/crew.js`, 마이그레이션 `2026-08-28_crew_feature.sql`.
> - **2단계 완료** — `GET /api/crew/{sno}/ranking`(+`rank.GetMemberSeasonScoresPort`), 게시판/랭킹 크루 배지(`crew.GetMemberCrewBadgePort` → `board`/`ranking` 응답에 크루명·아이콘, 스키마 변경 없음), 웹 렌더링. 공지 이력/서식은 보류.
> - **3단계**(땅따먹기 크루전)는 아래 그대로.

---

## 사전 결정 (열린 질문 → 권장안, MVP 기준)

| 항목 | 권장 |
|---|---|
| 한 회원 = 한 크루 | **예**. `crew_member.member_sno` 전역 UNIQUE로 강제 (땅따먹기 크루전 단위 명확화) |
| 가입 방식 | **승인제 하나만**. `join_policy` 컬럼은 두되 v1은 `APPROVAL`만 |
| 초대 권한 | **크루장만**. 부크루장/운영진은 후순위 |
| 크루명 | UNIQUE, 2~20자. 금칙어 필터는 후순위 |
| 회원 검색(초대용) | `member_id` 정확/접두 일치 + `nickname` 부분 일치 |
| FK 타입 | `member` 참조 컬럼은 `BIGINT` (= `record_track.uno`, `member_score.member_sno` 와 동일. `Member.sno` PK가 Integer여도 신규 테이블은 Long) |
| 알림 | v1은 **폴링** (`GET /api/crew/invitations/me`). 푸시 시스템 없음 |
| 인원 제한 | 기본 30, `paceleague.crew.member-limit-default` config |

---

## 1단계 — 크루 기본

### 백엔드: `crew` 신규 도메인 (`api/src/main/java/com/example/paceleague/crew/`, Clean Architecture)

| 구분 | 작업 |
|---|---|
| 엔티티 | `Crew` (name UNIQUE, iconUrl, description, notice, joinPolicy, memberLimit, leaderMemberSno, memberCount, status ACTIVE/DISBANDED, create/update) |
| | `CrewMember` (crewSno, memberSno, role LEADER/MEMBER, joinedAt) — `(crew_sno,member_sno)` + `member_sno` 전역 UNIQUE |
| | `CrewInvitation` (crewSno, inviterMemberSno, inviteeMemberSno, status PENDING/ACCEPTED/DECLINED/CANCELED/EXPIRED, createAt, expiresAt) |
| | `CrewJoinRequest` (crewSno, memberSno, status PENDING/APPROVED/REJECTED/CANCELED, message, createAt, decidedAt) |
| policy (순수) | `CrewNamePolicy` (길이·문자 검증), `CrewRolePolicy` (행위별 권한 판정 표), `CrewMembershipPolicy` (인원 제한·중복 소속·상태 전이 규칙), `CrewInvitationPolicy` (만료 판정) |
| port/in | `CrewCommandUseCase` (생성/수정/해체/공지), `CrewMembershipUseCase` (탈퇴/추방/위임), `CrewInvitationUseCase` (초대/수락/거절/취소), `CrewJoinRequestUseCase` (신청/승인/거절/취소), `GetCrewQueryUseCase` (검색/상세/내 크루) |
| port/out | `CrewRepositoryPort` (+ `findBySnoForUpdate` 비관적 락 — 인원수 갱신), `CrewMemberRepositoryPort`, `CrewInvitationRepositoryPort`, `CrewJoinRequestRepositoryPort` |
| 크로스도메인 port | `member.SearchMembersPort` (아이디/닉네임 검색 — **member 도메인에 신규**), `member.GetMemberNicknamePort`(기존), `rank.GetMemberTierPort`(기존 — 크루원 목록 티어 배지), `media.ResolveApprovedMediaPort` (신규 — media id로 APPROVED url 조회 + 소유자 확인, 크루 아이콘용) |
| service | 각 UseCase 구현. 가입 확정(초대 수락/신청 승인)은 `Crew` row 비관적 락 → 인원 제한 + 중복 소속 재확인 → `CrewMember` insert + `memberCount++` (`MemberScore.addScore` 동시성 패턴) |
| adapter/in/web | `CrewController` — 아래 API 초안대로. `@MemberSno` (검색·상세는 `required=false`) |
| adapter/out | 각 `*JpaRepository` + `*PersistenceAdapter` (얇은 위임). 검색은 native `@Query` (크루명 LIKE + 정렬) |
| config | `SecurityConfig` — `GET /api/crew/search`, `GET /api/crew/{sno}` permitAll 추가. `CorsConfig` — `/api/crew/**` (GET/POST/PATCH/DELETE + Authorization/Content-Type) + `/api/member/search` 등록 |
| | `CrewProperties` (`@ConfigurationProperties("paceleague.crew")`) — `member-limit-default`(30), `invitation-expire-days`(7), `name-min`/`name-max`(2/20) |
| 마이그레이션 | `docs/migrations/2026-XX-XX_crew_feature.sql` — 4개 테이블 CREATE |

### 백엔드: `member` 도메인 소폭 확장

- `GET /api/member/search?q=` — `member_id`/`nickname` 검색 (초대 대상 고르기).
  `SearchMemberResponse`(memberSno, memberId, nickname, tier). `SearchMembersPort` 로 crew에 노출.

### API 초안 (Notion 문서 §12)

```
POST   /api/crew                                  크루 생성 (명, 아이콘 media id, 소개, 가입방식)
GET    /api/crew/search?q=                        크루명 검색 (공개)
GET    /api/crew/{sno}                            크루 상세 (공개 정보; 크루원이면 공지·크루원목록·랭킹 포함)
GET    /api/crew/me                               내 크루 정보 (없으면 null)
PATCH  /api/crew/{sno}                            크루장: 명·아이콘·소개·가입방식·공지 수정
DELETE /api/crew/{sno}                            크루장: 해체
GET    /api/member/search?q=                      회원 아이디/닉네임 검색 (초대용)
POST   /api/crew/{sno}/invitations                크루장: 회원 초대 (invitee member sno)
GET    /api/crew/invitations/me                   내가 받은 초대 목록
POST   /api/crew/invitations/{id}/accept|decline  초대 수락/거절
DELETE /api/crew/invitations/{id}                 크루장: 초대 취소
POST   /api/crew/{sno}/join-requests              회원: 가입신청
GET    /api/crew/{sno}/join-requests              크루장: 신청 목록
POST   /api/crew/join-requests/{id}/approve|reject 크루장: 승인/거절
DELETE /api/crew/join-requests/{id}               회원: 신청 취소
DELETE /api/crew/{sno}/members/me                 크루원: 탈퇴
DELETE /api/crew/{sno}/members/{memberSno}        크루장: 추방
POST   /api/crew/{sno}/leader                     크루장: 위임 (대상 member sno)
GET    /api/crew/{sno}/ranking                    크루원 기록/점수 랭킹  (2단계)
```

### 웹 (`web/`)

| 작업 |
|---|
| 상단 nav에 **"크루"** 추가 (`index.html`/`territory.html`/신규 페이지 공통, i18n `navCrew`) |
| `crew.html` — 로드 시 `GET /api/crew/me` 분기: **크루 없음** → 검색 페이지(크루명 검색바, 결과 목록, "크루 만들기", 받은 초대 목록) / **크루 있음** → 정보 페이지(아이콘·크루명·소개, 공지, 크루원 목록[닉네임+티어 배지+크루장 표시]) |
| 크루장 관리 UI — 아이디 검색→초대, 신청 목록 승인/거절, 공지·정보 수정, 추방/위임, 해체 (정보 페이지 내 토글 패널) |
| 크루 아이콘 업로드 — `app.js`의 media presign→PUT→complete→poll 재사용 (`createPostEditor` 내부 로직 일부 함수 추출 필요) |
| 아이콘 미설정 시 크루명 첫 글자 + 색상 플레이스홀더 (`territory.html`의 `colorFor` 같은 해시 색상) |
| i18n 10개 언어 키 추가, `/js/*.js?v=` 캐시버스트 버전 올리기 |

---

## 2단계

| 작업 |
|---|
| **크루원 기록저장 랭킹** — `GET /api/crew/{sno}/ranking`. 기존 `ranking` 도메인 리더보드 쿼리를 `crew_member` 조인으로 크루 범위 필터. 집계 기준: 시즌 점수(`MemberScore`) 우선, 탭으로 누적거리/횟수 추가 검토 |
| **게시판·랭킹 크루 배지 (Notion §9)** — `board`: `PostSummaryResponse`/`PostDetailResponse`에 `authorCrewName`/`authorCrewIconUrl` 추가 (`authorTier`와 동일 방식, 목록은 배치 조회 — `crew.GetMemberCrewBadgePort` 신규). `ranking`: `getRanking`/`top10` 응답에 `crewName`/`crewIconUrl`. `web/index.html`·`post.html`·랭킹 위젯 렌더 반영 |
| 크루 탈퇴/해체 시 배지 즉시 제거 (조회 시점 계산이라 자연 반영, 캐시만 주의) |
| 공지 서식/이력 (`crew_notice` 별 테이블) — 필요 시 |

---

## 3단계 — 러닝 땅따먹기 연계 (땅따먹기 v2 설계 후)

| 작업 |
|---|
| `territory.owner` 를 개인 → 크루 귀속 모델로 확장 (기여도 합산 → 크루 점령) |
| 크루 정보 페이지에 "현재 점령 영역 / 크루전 순위 / 시즌 기여도" |
| 시즌 리셋 시 크루 점령 초기화 규칙 |
| 크루 탈퇴/해체 시 진행 중 기여도·점령 처리 |

---

## 공통 (모든 단계)

| 작업 |
|---|
| `docs/` 동기화 — `database.md`(4개 테이블), `domains.md`(crew 도메인 규칙·상태 전이), `architecture.md`(크로스도메인 포트: crew→member/rank/media), `api.md`(엔드포인트), `CLAUDE.md`(도메인 목록·SecurityConfig·CorsConfig) |
| 테스트 — policy 순수 유닛(이름 검증, 권한 표, 인원 제한, 상태 전이, 초대 만료) + service Mockito(가입 확정 시 인원 제한/중복 소속, 위임 시 role 교체, 해체 시 연쇄 삭제) |
| 앱 협의 — 초대/신청 화면, 크루 정보 화면은 앱 팀. 백엔드/웹 먼저, API 계약은 `docs/api.md` |

---

## 주의점

- **알림 없음** — 초대받은 회원은 앱/웹에서 `invitations/me` 를 봐야 안다. 푸시는 별도 작업.
- **연쇄 삭제** — 크루 해체 시 `crew_member`/`crew_invitation`/`crew_join_request` 전부 삭제. `board`처럼 서비스에서 삭제 순서 명시 (JPA cascade 안 씀 — 코드베이스 컨벤션).
- **동시성** — 마지막 자리 두 명이 동시에 수락/승인 → `Crew` row 비관적 락으로 직렬화. 단일 인스턴스 전제(스위퍼와 동일).
- **크루장 탈퇴는 위임 또는 해체 후에만** — 리더 없는 크루 방지.

---

## 데이터 모델 초안 (Notion §11)

| 테이블 | 주요 컬럼 |
|---|---|
| `crew` | sno, name(UNIQUE), icon_url(nullable), description, notice, join_policy, member_limit, leader_member_sno, member_count, status(ACTIVE/DISBANDED), create_at, update_at |
| `crew_member` | sno, crew_sno, member_sno, role(LEADER/MEMBER), joined_at — (crew_sno, member_sno) UNIQUE, member_sno 전역 UNIQUE |
| `crew_invitation` | sno, crew_sno, inviter_member_sno, invitee_member_sno, status, create_at, expires_at |
| `crew_join_request` | sno, crew_sno, member_sno, status, message(nullable), create_at, decided_at |
