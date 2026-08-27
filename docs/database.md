# 데이터베이스

MySQL, Spring Data JPA(Hibernate) 사용. 로컬은 `ddl-auto: update`, 운영은 `ddl-auto: validate`(운영에서는 스키마를 코드가 자동 변경하지 않음 — 마이그레이션은 수동/별도 도구 필요, 이 저장소에는 마이그레이션 스크립트가 없음).

엔티티 간 JPA `@ManyToOne`/`@OneToMany` 연관관계는 최소한으로만 사용되고(`MemberScore.member`만 존재, 그마저 `insertable=false, updatable=false`), 대부분의 조인은 각 도메인의 `uno`/`memberSno`/`memberSno` 같은 **FK 컬럼 값을 직접 저장하고 애플리케이션 레벨에서 조합**하는 방식입니다. Hibernate의 시각적 연관관계 매핑보다 명시적인 컬럼/쿼리를 선호하는 스타일.

## 테이블 개요

| 테이블 | 엔티티 | 설명 |
|---|---|---|
| `member` | `Member` | 회원 계정 |
| `record` | `Record` | 러닝 기록 원본 (기록 1건 = 1 row) |
| `record_track` | `RecordTrack` | 러닝 세션의 GPS 트랙(경로) 원본 — `record` 1건당 최대 1행, 좌표 배열은 `points_json`에 JSON 통째로 저장 |
| `score_rank` | `Rank` | 기록 1건당 산정된 점수 로그 (히스토리성 테이블) |
| `member_score` | `MemberScore` | 회원×시즌별 누적 점수/티어 |
| `season` | `Season` | 시즌 메타데이터 |
| `app_version_policy` | `AppVersionPolicy` | 플랫폼별 앱 버전/점검 정책 |
| `board` | `Board` | 커뮤니티 보드(카테고리) 목록, DDL로 시딩 |
| `post` | `Post` | 게시글 |
| `comment` | `Comment` | 댓글(1단계 중첩 — `parent_comment_sno`) |
| `post_vote` | `PostVote` | 게시글 추천/비추천 기록 |
| `comment_vote` | `CommentVote` | 댓글 추천/비추천 기록 |
| `media` | `Media` | 게시글 첨부(이미지/동영상/링크) — S3 업로드 상태 + Rekognition 모더레이션 결과 |

## `member`

| 컬럼 | 타입(Java) | 제약 |
|---|---|---|
| sno | Integer (PK, IDENTITY) | |
| member_id | String | not null, unique, max 50 |
| password_hash | String | not null, max 255 (BCrypt 해시) |
| nickname | String | max 50, nullable |
| email | String | max 50, nullable |
| created_at | Instant | not null |
| updated_at | Instant | not null, `@PreUpdate`로 자동 갱신 |

## `record`

| 컬럼 | 타입(Java) | 설명 |
|---|---|---|
| sno | Long (PK, IDENTITY) | |
| uno | Long, not null | `member.sno` FK 값 (JPA 연관관계 매핑 없이 값만 저장) |
| season | Long, not null | 저장 시점의 "현재 시즌" 번호 (`season.season`) 스냅샷 |
| distance_record | BigDecimal | 미터 단위 거리 |
| start_time | LocalDateTime | 기록 시작 시각 |
| end_time | LocalDateTime | 기록 종료 시각 |
| create_at / update_at | LocalDateTime | 애플리케이션 코드에서 직접 설정(`LocalDateTime.now()`), `@PreUpdate`는 update_at만 갱신 |
| utc_offset | String | 클라이언트 타임존 오프셋, 서버는 저장만 하고 계산에 사용하지 않음 |

## `record_track`

앱이 러닝 중 5분마다 `POST /api/record/gps`로 보내는 GPS 좌표 청크를 **러닝 1건 = 1행**으로 누적하는 세션 테이블. `record`와 `record_sno`로 느슨하게 연결(FK 미강제). 마이그레이션: [migrations/2026-08-27_record_gps_track.sql](./migrations/2026-08-27_record_gps_track.sql)(최초 생성) → [migrations/2026-08-27_record_track_streaming.sql](./migrations/2026-08-27_record_track_streaming.sql)(청크 누적용으로 확장).

- `status=ACTIVE`: 진행 중. 청크가 올 때마다 `points_json`에 좌표가 append되고 `record_sno`는 `null`.
- `status=FINISHED`: 앱이 `finished=true`를 보낸 시점에 `record` 1건이 생성되고 `record_sno`가 채워짐. 이후 청크는 무시.

| 컬럼 | 타입(Java) | 설명 |
|---|---|---|
| sno | Long (PK, IDENTITY) | |
| uno | Long, not null | `member.sno` FK 값 |
| record_sno | Long, **nullable** | 종료 전에는 `null`, `finished` 처리 시 채워짐 |
| client_run_id | String, not null, **unique** | 앱이 러닝 시작 시 만든 고유 ID. 그 러닝의 모든 청크가 같은 값. max 100 |
| status | String | `ACTIVE` / `FINISHED` |
| activity_type | String | `RUNNING` |
| started_at | LocalDateTime | 첫 좌표의 `recordedAt`(UTC) |
| ended_at | LocalDateTime | 마지막으로 저장된 좌표의 `recordedAt`(청크마다 갱신) |
| last_point_at | LocalDateTime | 마지막 저장 좌표 시각 = 다음 청크에서 이보다 이후 좌표만 받는 **중복 방지 워터마크** |
| last_lat / last_lng | BigDecimal | 마지막 저장 좌표 위경도 — 다음 청크 첫 좌표와의 거리를 이어 붙이기 위해 보관 |
| distance_meters | BigDecimal | 좌표에서 haversine으로 누적 계산한 총 이동 거리(미터) |
| point_count / chunk_count | Integer | 누적 좌표 수 / 누적 청크 수 |
| elapsed_duration_ms | Long | 종료 시 `ended_at - started_at`(ms) |
| schema_version | Integer | 페이로드 스키마 버전 |
| loc_requested_interval_ms / loc_distance_filter_meters / loc_algorithm_version | Integer / BigDecimal / String | 앱의 위치 수집 설정(`location` 블록, 보통 첫 청크만) |
| device_platform / device_app_version / device_app_build_number | String / String / Integer | `device` 블록(보통 첫 청크만) |
| points_json | String (LONGTEXT) | 지금까지 누적된 좌표 배열 JSON. 청크마다 파싱→append→재직렬화. `[{sequence,recordedAt,latitude,longitude,altitudeMeters,accuracyMeters,rawLatitude,rawLongitude}, ...]` |
| utc_offset | String | 앱이 보내면 종료 시 `record.utc_offset`으로 전달 |
| create_at / update_at | LocalDateTime | 애플리케이션 코드에서 직접 설정 |

## `score_rank` (엔티티명 `Rank`)

기록 1건이 저장될 때마다 1행씩 쌓이는 점수 히스토리. 이 값들의 합이 `member_score.total_score`로 누적됩니다.

| 컬럼 | 타입(Java) | 설명 |
|---|---|---|
| sno | Long (PK, IDENTITY) | |
| uno | Long | |
| score | Integer | 해당 기록의 최종 점수 (base+scaled+add 합계) |
| scaled_score | Integer | 페이스 보너스 부분만 |
| add_score | Integer | 주간 횟수 보너스 부분만 |
| create_at / update_at | LocalDateTime | |
| utc_offset | String | |

## `member_score`

회원 1명 × 시즌 1개 조합당 1행. 랭킹/티어 조회의 기준 데이터.

| 컬럼 | 타입(Java) | 설명 |
|---|---|---|
| sno | Long (PK, IDENTITY) | |
| member_sno | Long, not null | |
| season_sno | Long, not null | `season.sno` (season 번호가 아니라 season 테이블의 PK) |
| total_score | int, not null | 누적 점수, 신규 생성 시 초기값 1500 |
| tier | String(30), not null | `RankTier` enum name, 점수 변경 시마다 재계산되어 저장 |
| create_at / update_at | LocalDateTime | |
| utc_offset | String(50) | |

- `(member_sno, season_sno)` 동시 갱신은 `rank.application.port.out.MemberScoreRepositoryPort.findByMemberSnoAndSeasonSnoForUpdate`(어댑터 구현: `MemberScoreJpaRepository`)가 `PESSIMISTIC_WRITE` 락으로 보호합니다(레이스 컨디션으로 점수가 유실되지 않도록).
- DB 레벨 유니크 제약이 코드상 명시되어 있지는 않음(엔티티에 `@Table(uniqueConstraints=...)` 없음) — 애플리케이션 로직으로만 1인 1시즌 1행을 보장.

## `season`

| 컬럼 | 타입(Java) | 설명 |
|---|---|---|
| sno | Long (PK, IDENTITY) | `member_score.season_sno`가 참조하는 값 |
| season | Long | 시즌 번호 (`record.season`이 참조하는 값 — `sno`가 아님에 주의) |
| start_dt | Instant | "현재 시즌" 판정 기준(`ORDER BY start_dt DESC LIMIT 1`) |
| end_dt | Instant | 현재 코드에서 종료 판정에는 사용되지 않음 |

> **주의**: `record.season`은 `season.season`(시즌 번호) 값을 참조하고, `member_score.season_sno`는 `season.sno`(PK) 값을 참조합니다. 같은 시즌이라도 두 컬럼에 저장되는 값의 의미가 다르므로 조인 시 혼동하지 않도록 주의가 필요합니다.

## `app_version_policy`

| 컬럼 | 타입(Java) | 설명 |
|---|---|---|
| sno | Long (PK, IDENTITY) | |
| platform | String(20) enum(`ANDROID`/`IOS`) | 플랫폼별 정책 1행 가정 |
| latest_version | String(30), not null | |
| min_required_version | String(30), not null | 이 미만이면 강제 업데이트 |
| store_url | String(500) | |
| update_message | String(500) | |
| maintenance_yn | String(1) | `"Y"`/`"N"` |
| maintenance_message | String(500) | |
| create_at / update_at | LocalDateTime | |

## `board`

DDL로 시딩만 하고(자유게시판/질문/인증 3개), 생성/수정 API는 없음 — [migrations/2026-08-08_board_feature.sql](./migrations/2026-08-08_board_feature.sql) 참고.

| 컬럼 | 타입(Java) | 설명 |
|---|---|---|
| sno | Long (PK, IDENTITY) | |
| slug | String(50), UNIQUE | URL에 쓰이는 식별자 (`free`, `qna`, `verify`) |
| name | String(50) | 표시명 |
| description | String(255) | |
| display_order | int | 목록 정렬 순서 |
| create_at / update_at | LocalDateTime | |

## `post`

| 컬럼 | 타입(Java) | 설명 |
|---|---|---|
| sno | Long (PK, IDENTITY) | |
| board_sno | Long | `board.sno` FK 값 |
| member_sno | Long | 작성자, `member.sno` FK 값 |
| record_sno | Long, nullable | 첨부한 `record.sno` FK 값. 작성 시점에 작성자 본인 소유 기록인지만 검증하고(`RecordQueryService.getOne`) 이후엔 값만 저장 — 기록이 나중에 삭제돼도 `post` 쪽엔 반영되지 않으므로 조회 시(`GetRecordSummaryPort`) 없으면 `attachedRecord: null`로 응답. [migrations/2026-08-11_post_record_attachment.sql](./migrations/2026-08-11_post_record_attachment.sql) 참고. |
| title | String(200) | |
| content | String (TEXT) | |
| view_count | int | 조회할 때마다 원자적 `UPDATE ... SET view_count = view_count + 1`로 증가, 중복 방지 없음 |
| score | int | 추천(+1)/비추천(-1) 합계, 투표 시점에 `PESSIMISTIC_WRITE` 락으로 갱신 |
| create_at / update_at | LocalDateTime | |

## `comment`

| 컬럼 | 타입(Java) | 설명 |
|---|---|---|
| sno | Long (PK, IDENTITY) | |
| post_sno | Long | |
| member_sno | Long | 작성자 |
| parent_comment_sno | Long, nullable | `NULL`이면 최상위 댓글, 값이 있으면 답글. **1단계 중첩만 허용** — 답글이 가리키는 부모는 항상 최상위 댓글이어야 함(서비스 레이어에서 검증) |
| content | String(1000) | |
| score | int | |
| create_at / update_at | LocalDateTime | |

## `post_vote` / `comment_vote`

회원 1명당 게시글/댓글 1건에 대해 최대 1행. `vote_value`는 `1`(추천) 또는 `-1`(비추천).

| 컬럼 | 타입(Java) | 설명 |
|---|---|---|
| sno | Long (PK, IDENTITY) | |
| post_sno / comment_sno | Long | |
| member_sno | Long | |
| vote_value | int | `1` 또는 `-1` |
| create_at / update_at | LocalDateTime | |

**`(member_sno, post_sno)` / `(member_sno, comment_sno)`에 실제 DB `UNIQUE` 제약을 걸어둡니다.** 위 `member_score`가 "DB 유니크 제약 없이 앱 로직만으로 보장"하는 것과 다른 예외적 선택인데, 추천 중복 저장은 점수 조작으로 바로 이어지는 버그라 신규 테이블 도입 시점에 제약을 거는 비용이 나중에 정리하는 비용보다 훨씬 적기 때문입니다.

## `media`

게시글에 첨부하는 이미지/동영상/링크. `type`이 `IMAGE`/`VIDEO`면 S3 업로드 + Rekognition 모더레이션을 거치고, `LINK`면 업로드 없이 URL만 저장되고 생성 즉시 `APPROVED`. [migrations/2026-08-11_media_attachments.sql](./migrations/2026-08-11_media_attachments.sql) 참고, 인프라(S3 버킷/IAM) 배경은 [infra.md](./infra.md) 참고.

| 컬럼 | 타입(Java) | 설명 |
|---|---|---|
| sno | Long (PK, IDENTITY) | |
| member_sno | Long, not null | 업로드한 회원, `member.sno` FK 값 |
| post_sno | Long, nullable | 첨부된 게시글의 `post.sno` FK 값. 업로드 시점엔 아직 게시글이 없어 `NULL`이었다가 게시글 작성 시 연결됨 |
| type | String enum(`IMAGE`/`VIDEO`/`LINK`) | |
| status | String enum(`PENDING`/`APPROVED`/`REJECTED`) | `LINK`는 생성 즉시 `APPROVED`, `IMAGE`/`VIDEO`는 모더레이션 결과에 따라 결정 |
| s3_key | String, nullable | `IMAGE`/`VIDEO`만 값 있음(`LINK`는 `NULL`) |
| url | String, nullable | `APPROVED`가 되기 전까지 `NULL`(모더레이션 통과 전 URL을 노출하지 않기 위함). `LINK`는 생성 시 바로 채워짐 |
| mime_type / file_size_bytes | String / Long, nullable | `IMAGE`/`VIDEO`만 값 있음 |
| rekognition_job_id | String, nullable | 동영상 비동기 모더레이션(`StartContentModeration`) 잡 ID, 폴링(`GetContentModeration`)에 사용. 이미지는 동기 API라 값 없음 |
| moderation_reason | String, nullable | `REJECTED`일 때 감지된 라벨명(최대 500자로 자름) |
| create_at / update_at | LocalDateTime | |

**주의**: `REJECTED`(모더레이션 거부 또는 용량 초과)로 판정되면 S3 객체 자체를 즉시 삭제합니다(`s3:DeleteObject`) — 버킷의 `media/` prefix가 공개 읽기이므로, API가 URL을 응답에 노출하지 않더라도 객체가 남아있으면 키를 아는 사람이 직접 접근할 수 있기 때문입니다. `post_sno`에 DB 레벨 FK 제약은 없습니다(이 저장소의 다른 테이블과 동일하게 애플리케이션 레벨 조인).

## Redis

애플리케이션 코드가 사용하는 것은 refresh token 저장뿐입니다(`member.adapter.out.token.RedisRefreshTokenAdapter`).

| 키 패턴 | 값 | TTL |
|---|---|---|
| `refresh:<64자 랜덤 문자열>` | `memberSno` (문자열) | `app.jwt.refresh-token-ttl-seconds` (14일) |
