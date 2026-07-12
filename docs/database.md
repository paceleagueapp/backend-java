# 데이터베이스

MySQL, Spring Data JPA(Hibernate) 사용. 로컬은 `ddl-auto: update`, 운영은 `ddl-auto: validate`(운영에서는 스키마를 코드가 자동 변경하지 않음 — 마이그레이션은 수동/별도 도구 필요, 이 저장소에는 마이그레이션 스크립트가 없음).

엔티티 간 JPA `@ManyToOne`/`@OneToMany` 연관관계는 최소한으로만 사용되고(`MemberScore.member`만 존재, 그마저 `insertable=false, updatable=false`), 대부분의 조인은 각 도메인의 `uno`/`memberSno`/`memberSno` 같은 **FK 컬럼 값을 직접 저장하고 애플리케이션 레벨에서 조합**하는 방식입니다. Hibernate의 시각적 연관관계 매핑보다 명시적인 컬럼/쿼리를 선호하는 스타일.

## 테이블 개요

| 테이블 | 엔티티 | 설명 |
|---|---|---|
| `member` | `Member` | 회원 계정 |
| `record` | `Record` | 러닝 기록 원본 (기록 1건 = 1 row) |
| `score_rank` | `Rank` | 기록 1건당 산정된 점수 로그 (히스토리성 테이블) |
| `member_score` | `MemberScore` | 회원×시즌별 누적 점수/티어 |
| `season` | `Season` | 시즌 메타데이터 |
| `app_version_policy` | `AppVersionPolicy` | 플랫폼별 앱 버전/점검 정책 |

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

- `(member_sno, season_sno)` 동시 갱신은 `MemberScoreRepository.findByMemberSnoAndSeasonSnoForUpdate`가 `PESSIMISTIC_WRITE` 락으로 보호합니다(레이스 컨디션으로 점수가 유실되지 않도록).
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

## Redis

애플리케이션 코드가 사용하는 것은 refresh token 저장뿐입니다(`RefreshTokenService`).

| 키 패턴 | 값 | TTL |
|---|---|---|
| `refresh:<64자 랜덤 문자열>` | `memberSno` (문자열) | `app.jwt.refresh-token-ttl-seconds` (14일) |
