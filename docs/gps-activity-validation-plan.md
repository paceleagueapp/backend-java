# GPS 러닝 진위 검증 설계 (자전거/스쿠터/차량 판별)

> **상태** (2026-09-09): **설계 문서만. 구현 없음.** 사용자와 대화로 방향만 확정한 것을 문서화.
>
> 핵심 방침: **LLM 아님.** 규칙 기반 휴리스틱 → (데이터 쌓이면) GBDT. **자동 반려·점수 삭제 안 함 — 플래그(advisory)만 달고 운영자 검토.**
> 의심(반려 대상) 러닝은 전용 테이블 `record_activity_review`에 쌓아 검토 큐 + 조치 이력으로 관리.

## 배경 / 문제

`POST /api/record/gps`로 들어오는 러닝은 GPS 좌표 시계열이다. 사용자가 실제로 뛴 게 아니라
**자전거·전동킥보드·스쿠터·자동차**를 타고 이동하면서 러닝으로 기록하면:

- 개인 랭킹(`member_score`) 점수를 부정 획득
- **러닝 땅따먹기(`territory`)** — 자전거로 큰 폐곡선을 빠르게 그려 남의 땅을 대량 점령
- 크루 랭킹·통계 왜곡

현재는 아무 검증이 없다. `GpsSessionValidator`는 **좌표 형식**(범위, 개수 상한, 타임스탬프 존재)만 보고
"이 이동이 러닝답나"는 보지 않는다.

## 목표 / 비목표

**목표**
- 러닝 1건이 끝날 때 "사람이 뛴 것"인지 **의심도 점수**(`suspicionScore`, 0~100)를 매긴다.
- 명백한 비러닝(지속 25km/h↑ 등)은 높은 점수로 뜨게 한다.
- 운영자가 검토할 수 있는 큐/쿼리를 제공한다.
- 나중에 학습 모델로 갈 수 있도록 **피처를 러닝마다 저장**해 둔다.

**비목표 (명시적으로 안 함)**
- 러닝 자동 반려 / 점수 자동 삭제 / 실시간 차단. 오탐으로 정상 유저 기록을 지우는 리스크가 부정 이용보다 크다.
- 청크 수신 중(러닝 진행 중) 실시간 판정. 종료 시점 배치성 1회 계산으로 충분.
- 100% 정확도. 빠른 러너(18km/h 스프린트)와 느린 자전거(15km/h 산책)는 경계가 흐리다 — 그건 운영자 판단.
- 워킹/조깅 구분. "동력 이동체 vs 사람 다리"만 본다.

## 아키텍처 위치

- **`record.domain.policy.ActivityAuthenticityAnalyzer`** (신규, 순수 계산 클래스, 단위 테스트) —
  좌표 배열 + 시간 → `ActivityAuthenticityResult(suspicionScore, List<SuspicionSignal>, feature map)`.
  기존 `GeoDistanceCalculator`(haversine)를 그대로 재사용. Spring 의존성 0.
- **호출 지점**: `SaveGpsSessionService.finalizeRun` — `RecordUseCase.create` 성공 후, `territory` 처리와
  같은 위치에서 **best-effort**(예외를 삼키고 로깅). 판정 실패가 러닝 저장을 롤백하면 안 된다.
- **저장**: 아래 스키마. `record` 도메인 안에서 끝나므로 크로스도메인 포트 불필요.
- **조회/검토**: 의심 러닝은 전용 테이블 `record_activity_review`에 쌓이고, v1은 **엔드포인트 없이 운영자 DB
  쿼리**로 검토·조치(관리자 인증 체계가 아직 없음 — 전부 일반 회원 JWT. `board_report`도 같은 이유로 관리자
  UI 없이 감). 관리자 API/화면은 `board_report` + 이걸 묶어서 별도 과제.

## 스키마

두 갈래로 나눈다:
1. **분석된 모든 러닝** — `record_track`에 경량 점수 컬럼(분포 관측·튜닝·GBDT 학습셋용).
2. **의심 러닝(반려/검토 대상)만** — 전용 테이블 `record_activity_review`에 1행. 검토 큐 + 조치 이력.

의심 러닝은 전체의 극소수라, 검토·관리 로직을 전용 테이블에 두면 쿼리가 항상 작고(인덱스 스캔 없이 전체 조회 가능),
"조치했는지 / 누가 / 언제 / 무엇을"을 붙이기 자연스럽다. `board_report`와 같은 패턴.

```sql
-- docs/migrations/YYYY-MM-DD_gps_activity_validation.sql

-- 1) 모든 분석 러닝: 점수만 (관측/학습용)
ALTER TABLE record_track
    ADD COLUMN suspicion_score   INT           NULL AFTER territory_mode,  -- 0~100, NULL=미분석/스킵
    ADD COLUMN suspicion_signals VARCHAR(500)  NULL AFTER suspicion_score, -- 발동 신호 코드 CSV
    ADD COLUMN feature_json      VARCHAR(2000) NULL AFTER suspicion_signals; -- 원시 피처 스냅샷(GBDT 학습셋 캐시)
CREATE INDEX idx_record_track_suspicion ON record_track (suspicion_score);

-- 2) 의심 러닝만: 검토 큐 + 조치 이력
CREATE TABLE record_activity_review (
    sno              BIGINT       NOT NULL AUTO_INCREMENT,
    record_track_sno BIGINT       NOT NULL,                     -- 원본 트랙
    record_sno       BIGINT       NULL,                         -- 확정된 record (있으면)
    member_sno       BIGINT       NOT NULL,                     -- = record_track.uno
    suspicion_score  INT          NOT NULL,
    suspicion_signals VARCHAR(500) NULL,
    feature_json     VARCHAR(2000) NULL,                        -- 검토 시점 스냅샷(원본이 지워져도 남음)
    detected_at      DATETIME     NOT NULL,                     -- 러닝 종료(분석) 시각
    territory_mode   TINYINT(1)   NOT NULL DEFAULT 0,           -- 땅따먹기 러닝이었나(우선순위 판단)

    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',   -- PENDING / CLEARED / CONFIRMED
    action_taken     VARCHAR(30)  NULL,                         -- NONE / SCORE_REVERTED / TERRITORY_REVOKED / ACCOUNT_WARNED / ACCOUNT_SUSPENDED (CSV 가능)
    reviewer_note    VARCHAR(500) NULL,
    reviewed_by      VARCHAR(50)  NULL,                         -- 운영자 식별자(수동 기입)
    reviewed_at      DATETIME     NULL,

    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME     NOT NULL,
    PRIMARY KEY (sno),
    UNIQUE KEY uq_review_track (record_track_sno),              -- 트랙 1건당 검토 1행(재분석은 갱신)
    KEY idx_review_status (status, suspicion_score),
    KEY idx_review_member (member_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- `ddl-auto: validate` — 배포 전 이 SQL을 `paceleague-db` EC2에 직접 실행.
- 기존 러닝은 `suspicion_score = NULL`(미분석), `record_activity_review` 비어 있음. 소급 분석은 별도 배치(`territory` 히스토리 replay 패턴) — 돌리면 의심 건이 `record_activity_review`에 쌓임.
- `record_track`에는 `review_status`를 **두지 않는다** — 검토 상태는 `record_activity_review`가 단일 출처(SSOT). 트랙에서 "이거 검토중?"이 궁금하면 `record_activity_review`를 조인. (같은 값을 두 곳에 두면 동기화 버그.)
- FK 제약은 이 저장소 관례대로 안 검. `record_activity_review` 행은 원본 트랙/레코드가 지워져도 남아야 하므로(부정 이력 보존) `feature_json`·`suspicion_*`를 이 테이블에도 복사 저장.

### 엔티티 / 도메인

- `record.domain.entity.RecordActivityReview` (`@Entity`, 정적 팩토리 `open(...)` + 뮤테이터 `clear(note, by)` / `confirm(actions, note, by)` / `reReview(score, signals)`).
- `record.application.port.out.RecordActivityReviewRepositoryPort` + `...JpaRepository` + `...PersistenceAdapter`.
- `SaveGpsSessionService.finalizeRun`: 분석 후 `suspicionScore >= review-threshold`면
  `recordActivityReviewRepositoryPort.upsert(RecordActivityReview.open(...))` — 이미 있으면(재분석) `reReview`.
- 조치 실행(점수 차감/땅 회수)은 v1에서 코드로 안 함 — `action_taken`은 운영자가 수동으로 한 걸 **기록만**.

## 1단계 — 규칙 기반 휴리스틱

좌표에서 세그먼트별 속도(`v_i = haversine(p_i, p_{i+1}) / Δt`)를 만들고, 아래 신호를 계산해 가중합.
모든 임계값은 `RecordProperties`(또는 신규 `paceleague.gps.authenticity.*` `@ConfigurationProperties`)로 빼서
yml 없이 코드 기본값 + 필요 시 운영 오버라이드.

| 코드 | 신호 | 계산 | 기본 임계 | 가중치 |
|---|---|---|---|---|
| `SUSTAINED_SPEED` | 지속 고속 | 5분 이상 롤링 평균 속도 | > 20 km/h | 40 |
| `PEAK_SPEED` | 순간 초고속 | 이상치 제거 후 최고 세그먼트 속도 | > 32 km/h | 35 |
| `LOW_SPEED_VARIANCE` | 속도 변동 없음 | 이동 구간 속도의 변동계수(CV) | CV < 0.12 | 20 |
| `SMOOTH_ACCEL` | 매끄러운 가감속 | 저크(속도 2차 미분) RMS가 비정상적으로 낮음 | 하위 기준 미달 | 15 |
| `STOP_GO_PATTERN` | 신호 정지-급출발 | 완전 정지(≈0) 후 3초 내 15km/h 도달 이벤트 수 | ≥ 4회 | 15 |
| `ROAD_SNAP` | 도로만 추종 | 경로가 차도 그래프에 얼마나 붙는지(옵션, 외부 지도 데이터 필요) | — | 10 |
| `UPHILL_SPEED` | 오르막 속도 유지 | 고도 상승 구간에서도 속도가 안 떨어짐 | 상관계수 > -0.1 | 10 |
| `IMPLAUSIBLE_PACE` | 인간 한계 초과 | 전체 평균 페이스 | < 2:50 /km | 50 |

- **합산 점수** = min(100, Σ 가중치). `suspicion_signals`에 발동된 코드 CSV 저장.
- `feature_json`에는 임계 통과 여부가 아니라 **원시 피처값**(avgSpeed, p95Speed, speedCV, jerkRms, stopGoCount, meanPace, distanceKm, movingRatio, elevGain, pointDensity …)을 저장.
- **정확도 보정**: `accuracyMeters`가 큰(>30m) 좌표, `Δt`가 너무 큰(>15s, 터널·신호 손실) 세그먼트는
  속도 계산에서 제외하거나 다운웨이트. GPS 튐(순간 200km/h 1개 점)은 이상치 제거로 걸러 `PEAK_SPEED` 오탐 방지.
- **짧은 러닝**(거리 < 1km 또는 좌표 < 30개)은 신뢰구간이 넓어 분석 스킵(`suspicion_score` NULL 유지).

**임계 처리**
- 모든 분석 러닝: `record_track.suspicion_score`/`signals`/`feature_json` 기록.
- `suspicionScore >= review-threshold`(기본 60) → `record_activity_review`에 `status = PENDING` 행 생성(upsert).
- **어느 경우든 러닝·점수·땅은 정상 처리** (advisory). 반려는 운영자가 `record_activity_review`에서 `CONFIRMED` 처리 + 조치.

## 2단계 — GBDT (데이터 축적 후)

- 라이브러리: LightGBM 또는 XGBoost. **서버 인라인 추론이 아니라** 배치 학습 → 경량 모델 파일 →
  Java에서 로드(예: `xgboost4j`, 또는 모델을 규칙 트리로 export). 추론은 러닝당 µs 단위.
- **학습셋**: 1단계에서 `PENDING` 뜬 것 + 명백 정상 샘플을 운영자가 `CLEARED`/`CONFIRMED`로 라벨링 →
  `feature_json` + 라벨로 학습. 초기엔 규칙으로 약라벨(weak label) 부트스트랩.
- 출력은 그대로 `suspicion_score`(모델 확률 × 100)로 덮어씀 — 스키마·검토 플로우 불변, 계산기만 교체.
- 재학습 주기: 월 1회 수동. 자동 파이프라인은 과잉(인프라 최소 원칙).
- **모델 드리프트·오탐 감시**: `CONFIRMED` 대비 `CLEARED` 비율, 스코어 분포를 운영 쿼리로 주기 확인.

## 운영자 검토 플로우 (v1 = DB 쿼리)

모든 조회·갱신이 `record_activity_review` 한 테이블에서 끝난다(작음).

```sql
-- 검토 대기 목록 (점수 높은 순, 땅따먹기 러닝 우선)
SELECT r.sno, r.member_sno, r.suspicion_score, r.suspicion_signals,
       r.territory_mode, r.detected_at, r.record_sno, r.record_track_sno
FROM record_activity_review r
WHERE r.status = 'PENDING'
ORDER BY r.territory_mode DESC, r.suspicion_score DESC;

-- 특정 회원의 이력 (반복 위반자 판단)
SELECT status, suspicion_score, action_taken, detected_at
FROM record_activity_review WHERE member_sno = ? ORDER BY detected_at DESC;

-- 정상 판정
UPDATE record_activity_review
SET status='CLEARED', reviewer_note=?, reviewed_by=?, reviewed_at=NOW(), updated_at=NOW()
WHERE sno = ?;

-- 부정 확정 (+ 운영자가 실제로 한 조치를 기록)
UPDATE record_activity_review
SET status='CONFIRMED', action_taken='SCORE_REVERTED,TERRITORY_REVOKED',
    reviewer_note=?, reviewed_by=?, reviewed_at=NOW(), updated_at=NOW()
WHERE sno = ?;
```

- `CONFIRMED` 시 실제 조치(점수 차감, 땅 회수, 계정 경고/정지)는 **v1 범위 밖 — 운영자가 손으로 하고
  `action_taken`에 무엇을 했는지 남긴다**. 자동 실행(`RevertRecordScorePort` 등)은 오탐 리스크가 커서 뒤로 미룸.
- 반복 위반자 자동 제재는 `idx_review_member`로 이력을 뽑아 판단하되, 역시 v1은 수동.

## territory(땅따먹기) 연계

- `ProcessTerritoryRunService`는 지금 `record_track`을 모른다(좌표를 값으로 받음). 연계하려면
  `finalizeRun`에서 **먼저** 진위 분석 → `suspicionScore`를 `ProcessTerritoryRunCommand`에 실어 전달.
- **옵션 A (권장, v1)**: 땅따먹기도 그냥 진행하되 `territory`에 `suspicion_score` 스냅샷을 남겨
  나중에 `CONFIRMED` 시 그 땅들을 운영자가 회수.
- **옵션 B**: `suspicionScore >= hard-threshold`(예: 85, 거의 확실)면 그 러닝의 **땅 점령만 보류**
  (개인 점수는 그대로). 경쟁 기능이라 오탐 비용이 상대적으로 낮음. — 사용자 결정 필요.

## 공통 인프라 영향

- 스케줄 잡 없음. 종료 시점 동기 계산(순수 CPU, 좌표는 이미 메모리에).
- 마이그레이션 1개: `record_track` 컬럼 3개 + 인덱스, `record_activity_review` 테이블 생성.
- 새 config 네임스페이스 `paceleague.gps.authenticity.*` (임계값, 가중치, review-threshold, enabled 플래그).
  `enabled=false`면 계산 자체를 스킵 → 완전 무해하게 배포 가능.
- 외부 의존: 1단계는 없음. `ROAD_SNAP` 신호와 2단계는 지도 데이터/ML 라이브러리 필요 — 나중에.
- **단일 인스턴스 제약 없음** (상태 없는 계산).

## 구현 순서 (권장)

1. 마이그레이션 파일 작성(`record_track` 컬럼 + `record_activity_review`).
2. `ActivityAuthenticityAnalyzer` + 단위 테스트 — 실제 러닝/자전거 GPS 로그 몇 개로 회귀 테스트 픽스처 구성.
3. `RecordProperties`에 `authenticity` 블록(임계·가중·enabled).
4. `RecordActivityReview` 엔티티 + 포트/어댑터.
5. `SaveGpsSessionService.finalizeRun`에 best-effort 호출 → `record_track` 점수 저장 + 임계 초과 시 `record_activity_review` upsert.
6. `GpsSessionSweeper` 자동 마감 경로에도 동일 적용(같은 `finalizeRun` 통과하면 자동).
7. 운영 쿼리 문서화(이 문서 or `docs/ops-queries.md`).
8. (관측 후) 임계값 튜닝 → GBDT 검토.

## 열린 결정사항

- **`review-threshold` 기본값** (60? 70?). 오탐률 보고 조정.
- **땅따먹기 옵션 A vs B** (의심 러닝의 땅 점령을 보류할지).
- `CONFIRMED` 시 **점수·땅 자동 회수 여부** (v1은 수동 + `action_taken` 기록만, 자동화는 반복 위반자 한정?).
- `feature_json` 크기 — 학습셋이 커지면 `VARCHAR(2000)` → 별도 `record_activity_feature` 테이블 or JSON 컬럼.
- **소급 분석**: 기존 러닝 전체를 배치로 돌려 `record_activity_review`를 채울지, 신규만 볼지.
- 모바일 앱에 판정 결과를 노출할지 (`GpsSessionResponse`에 필드 추가) vs 서버 내부에만 둘지 —
  노출하면 부정 유저가 임계를 역산해 회피 학습함. **비노출 권장.**
- 워치/폰 **가속도계·`activityType`(Health Connect)** 데이터를 앱이 같이 올려주면 판별력이 크게 오름 —
  앱 팀과 협의 대상(현재 `activityType`은 `RUNNING` 고정으로만 옴).
