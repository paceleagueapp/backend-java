# 매일 아침 땅따먹기 요약 FCM 푸시 — 서버 설계

> 목표: **매일 09:00(KST)에 전체 회원에게 "어제 N명이 M개의 땅을 점령했어요" FCM 푸시**.
> 이 문서는 **서버(api/) 부분만** 다룬다. 앱(FCM SDK 연동, 토큰/토픽 구독, 알림 권한, 딥링크)과
> Firebase 프로젝트 준비는 별도.

현재 코드베이스에는 **FCM·푸시·디바이스 토큰 관련 코드가 전혀 없다.** 인프라부터 신규 구축한다.

---

## 사전 결정 (열린 질문 → 권장안, MVP 기준)

| 항목 | 권장안 | 비고 |
|---|---|---|
| 발송 대상 관리 | **v1: FCM 토픽(`all`) 브로드캐스트** | 토큰 테이블·배치 불필요. 개인 알림 필요해지면 phase 2에서 토큰 테이블 추가 |
| "점령한 땅" 정의 | `territory` 행 중 `create_at`이 어제 범위인 것의 **개수** | 2026-09-07 규칙상 territory-mode 러닝 1회 = 최대 1개 신규 `territory` 행. 뺏김으로 삭제된 행은 카운트 안 함(= "점령 이벤트" 수) |
| "점령한 유저" 정의 | 위 행들의 `COUNT(DISTINCT owner_member_sno)` | |
| 0건인 날 | **푸시 스킵** (로그만 남김) | 문구 결정 여지 있음 — "어제는 조용했어요" 발송도 가능 |
| 타임존 | 집계는 **KST 기준 '전날'**, `@Scheduled(zone="Asia/Seoul")` | `territory.create_at` 저장 TZ 확인 필수 (아래 §5 ⚠️) |
| 다중 인스턴스 | 현재 1대 전제. **중복 발송 가드(발송 로그) 필수** | 스케일아웃 시 ShedLock. sweeper와 달리 중복 시 전 회원 2회 푸시라 체감 큼 |
| 수신 동의/거부 | 토픽 구독/해제로 처리(앱). 서버 opt-out 목록은 phase 2 | 09:00 발송이라 야간 수신 동의(정통망법)는 불필요. opt-out 수단은 갖출 것 |
| i18n | v1은 한국어 고정 | `member`에 언어 필드 없음. phase 2에서 언어별 토픽(`all_ko`/`all_en`) |
| FK 타입 | `member` 참조 컬럼은 `BIGINT` | 크루/랭킹 테이블과 동일 컨벤션 |

---

## 1. 아키텍처 개요

신규 도메인 `notification` (`api/src/main/java/com/paceleague/notification/`, Clean Architecture):

```
notification/
  config/
    FcmProperties.java            @ConfigurationProperties("paceleague.fcm")
    FirebaseConfig.java           FirebaseApp 초기화 (@Bean)
  application/
    port/in/
      SendDailyTerritoryDigestUseCase.java
    port/out/
      SendPushPort.java           토픽/토큰 발송 추상화
    service/
      DailyTerritoryDigestService.java
    dto/
      DailyTerritoryDigest.java   (capturedTerritories, distinctOwners, date)
  adapter/
    in/scheduler/
      DailyTerritoryDigestScheduler.java   @Scheduled(cron, zone="Asia/Seoul")
    out/push/
      FcmPushAdapter.java         implements SendPushPort (firebase-admin)
    out/persistence/
      PushSendLogJpaRepository.java + PushSendLogPersistenceAdapter.java
```

교차 도메인 (기존 패턴 준수):

- `territory.application.port.in.shared.CountTerritoryCapturesPort` — territory 도메인이 노출하는 집계 포트.
  `notification` → `territory` 방향 (record→rank, board→crew 와 동일).
  - `TerritoryQueryService`(또는 신규 소형 서비스)가 구현, `TerritoryJpaRepository`에 집계 쿼리 추가.
- `member` 조회는 v1 토픽 방식이면 **불필요**. (phase 2 토큰 방식일 때 `member.GetActiveDeviceTokensPort`)

`common.config.SchedulingConfig`의 `@EnableScheduling`은 그대로 재사용.
`FcmProperties`는 `PaceleagueApplication`의 `@EnableConfigurationProperties`에 추가.

---

## 2. 데이터 모델 / 마이그레이션

### v1 (토픽 방식) — `push_send_log` 만 추가

```sql
-- docs/migrations/YYYY-MM-DD_push_send_log.sql
CREATE TABLE push_send_log (
    sno           BIGINT       NOT NULL AUTO_INCREMENT,
    kind          VARCHAR(40)  NOT NULL,           -- 'DAILY_TERRITORY_DIGEST'
    target_date   DATE         NOT NULL,           -- 집계 대상 '전날' (KST)
    title         VARCHAR(200) NOT NULL,
    body          VARCHAR(500) NOT NULL,
    payload_json  VARCHAR(1000) NULL,
    fcm_message_id VARCHAR(200) NULL,              -- 토픽 발송 성공 시 messageId
    status        VARCHAR(20)  NOT NULL,           -- SENT / SKIPPED / FAILED
    detail        VARCHAR(500) NULL,               -- 실패 사유 / 스킵 사유(0건 등)
    created_at    DATETIME     NOT NULL,
    PRIMARY KEY (sno),
    UNIQUE KEY uq_push_send_log_kind_date (kind, target_date)   -- 하루 1건 = 중복 발송 가드
);
```

`(kind, target_date)` UNIQUE가 **다중 인스턴스/재시작 시 중복 발송을 막는 핵심 가드**.
스케줄러는 "행 INSERT(status=SENDING) 시도 → 이미 있으면 skip → 발송 → status UPDATE" 순서.

### phase 2 (토큰 방식, 개인 알림 시) — 추가로

```sql
CREATE TABLE member_device_token (
    sno          BIGINT       NOT NULL AUTO_INCREMENT,
    member_sno   BIGINT       NOT NULL,
    token        VARCHAR(255) NOT NULL,
    platform     VARCHAR(10)  NOT NULL,            -- ANDROID / IOS
    push_enabled TINYINT(1)   NOT NULL DEFAULT 1,
    last_seen_at DATETIME     NOT NULL,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME     NOT NULL,
    PRIMARY KEY (sno),
    UNIQUE KEY uq_device_token (token),
    KEY idx_device_token_member (member_sno)
);
```

---

## 3. API (phase 2 — 토큰 방식일 때만)

v1 토픽 방식은 **서버 API 불필요** (앱이 FCM 토픽 구독).

phase 2:

| 메서드 | 경로 | 설명 |
|---|---|---|
| `POST` | `/api/member/device-token` | 로그인 필요. body `{ token, platform }`. token 기준 upsert(`member_sno` 갱신, `last_seen_at` 갱신) |
| `DELETE` | `/api/member/device-token` | 로그인 필요. body `{ token }`. 로그아웃/알림 끄기 시 |

- `MemberController`(`/api/member`)에 추가하거나 `notification` 도메인 컨트롤러 분리.
- `SecurityConfig` permitAll 대상 아님(인증 필요) — 기본 정책 그대로.
- CORS 불필요(앱 전용).

---

## 4. 집계 쿼리

`TerritoryJpaRepository`에 네이티브 집계 1개:

```java
@Query(value = """
        select count(*)                        as capturedTerritories,
               count(distinct owner_member_sno) as distinctOwners
        from territory
        where create_at >= :from and create_at < :to
        """, nativeQuery = true)
TerritoryCaptureCountProjection countCapturesBetween(@Param("from") LocalDateTime from,
                                                     @Param("to") LocalDateTime to);
```

- `status` 조건은 넣지 않는다: 어제 만들어졌다가 오늘 뺏겨서 삭제된 행은 이미 없고, 삭제 안 됐으면 대부분 ACTIVE.
  "어제 발생한 점령 이벤트 수"가 목적이므로 `create_at` 범위만으로 충분.
- (대안 지표) `sum(hex_count)` 도 같이 뽑아 "M칸" 표현 가능 — 문구 결정에 따라.
- `TerritoryQueryService`(이미 `GetTerritoryRankingUseCase` 구현) 또는 신규 소형 서비스가
  `CountTerritoryCapturesPort` 구현. `notification`은 이 포트만 의존.

---

## 5. 타임존 ⚠️ (반드시 확인)

- `Territory.createAt = LocalDateTime.now()` — **서버 시스템 기본 TZ** 기준.
- Docker 이미지(`eclipse-temurin:21-jre`)에 TZ 미설정 → **컨테이너는 UTC**로 추정.
  → `territory.create_at`은 **UTC 벽시계값**으로 저장돼 있을 가능성 높음.
- 따라서 "어제(KST)" 범위를 `create_at` 저장 TZ(= UTC 가정)로 변환해서 질의해야 함. 계산:
  ```
  SEOUL       = ZoneId.of("Asia/Seoul")
  yesterday   = LocalDate.now(SEOUL).minusDays(1)
  fromInstant = yesterday.atStartOfDay(SEOUL).toInstant()
  toInstant   = yesterday.plusDays(1).atStartOfDay(SEOUL).toInstant()
  from        = LocalDateTime.ofInstant(fromInstant, ZoneOffset.UTC)   // 쿼리 파라미터
  to          = LocalDateTime.ofInstant(toInstant,   ZoneOffset.UTC)
  ```
  예) 2026-09-09 09:00 KST 발화 → `yesterday`=2026-09-08(KST) → `from`=2026-09-07 15:00 UTC, `to`=2026-09-08 15:00 UTC. `to`는 발화 시점보다 9시간 과거라 경계 안전.
- **선행 작업**: 운영 DB에서 확인
  1. `SELECT NOW(), @@global.time_zone, @@session.time_zone;`
  2. 최근 territory 몇 건의 `create_at` 을 실제 점령 시각과 대조 → UTC인지 KST인지 확정
  3. 그 결과에 맞춰 스케줄러의 window 계산 + 단위 테스트 고정
- `@Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")` 의 `zone` 은 크론 발화 시각만 KST로 맞춰줌 — 집계 window 계산과는 별개.

---

## 6. 스케줄러

`DailyTerritoryDigestScheduler` (`adapter/in/scheduler`, `GpsSessionSweeper` 와 같은 자리·패턴):

```java
@Component
@ConditionalOnProperty(name = "paceleague.fcm.daily-digest.enabled", havingValue = "true", matchIfMissing = false)
public class DailyTerritoryDigestScheduler {

    @Scheduled(cron = "${paceleague.fcm.daily-digest.cron:0 0 9 * * *}", zone = "Asia/Seoul")
    public void run() {
        sendDailyTerritoryDigestUseCase.sendForYesterday();
    }
}
```

- **기본 OFF** (`matchIfMissing = false`) — `application-prod.yml` 에서 명시적으로 켬.
  (sweeper는 matchIfMissing=true지만, 마케팅성 전체 발송은 실수로 켜지지 않게 반대로.)
- 크론 오버라이드 가능하게 property 로.

서비스 로직 (`DailyTerritoryDigestService.sendForYesterday()`):

1. `targetDate` = 어제(KST) 계산
2. `pushSendLogRepository` 에 `(DAILY_TERRITORY_DIGEST, targetDate)` **INSERT 시도** — `DataIntegrityViolationException`(UNIQUE 충돌)이면 "이미 처리됨" 로그 후 return. (= 중복 발송 가드)
3. `countTerritoryCapturesPort.countBetween(from, to)` 집계
4. `capturedTerritories == 0` 이면 로그 status=`SKIPPED`, detail="0 captures" 후 return
5. 문구 조립 (§7)
6. `sendPushPort.sendToAll(title, body, data)` → `FcmPushAdapter` 가 `Message.builder().setTopic("all")...send()`
7. 성공: 로그 status=`SENT`, `fcm_message_id` 저장 / 실패: status=`FAILED`, detail=예외메시지 (예외는 삼켜서 다음 날 재시도 가능하게)

---

## 7. 알림 페이로드

| 필드 | 값(예시) |
|---|---|
| `notification.title` | `땅따먹기 어제 요약` |
| `notification.body` | `어제 42명이 128개의 땅을 점령했어요 🏴` |
| `data.type` | `daily_territory_digest` |
| `data.date` | `2026-09-08` |
| `data.deeplink` | `paceleague://territory` (앱 라우팅 규약은 앱팀 협의) |

- Android/iOS 공통은 `notification` + `data` 동시 지정.
  포그라운드에서 커스텀 처리하려면 `data`-only 도 고려(앱 협의 사항).
- 문구 상수는 코드에 두되, 숫자 포맷(`String.format`)만 주입.
- i18n: v1 한국어 고정. phase 2에서 언어별 토픽 → 언어별 문구 맵.

---

## 8. FCM 발송 어댑터

### v1 토픽

```java
FirebaseMessaging.getInstance().send(
    Message.builder()
        .setTopic("all")
        .setNotification(Notification.builder().setTitle(title).setBody(body).build())
        .putAllData(data)
        .build());
```

- 반환 `messageId` 를 로그에 저장.
- 예외 타입: `FirebaseMessagingException` — `getMessagingErrorCode()` 로 분기(대부분 재시도 불가, 로깅만).

### phase 2 토큰 (참고)

- `FirebaseMessaging.sendEachForMulticast(MulticastMessage)` — **한 번에 최대 500 토큰**.
- 전체 토큰을 500개씩 배치 루프. 각 배치 응답의 `getResponses()` 순회:
  - `UNREGISTERED` / `INVALID_ARGUMENT` → 해당 `member_device_token` 삭제(정리)
- 발송 로그에 `sent_count` / `failure_count` 추가.

---

## 9. 설정 / 시크릿

`application.yml` (공통):

```yaml
paceleague:
  fcm:
    daily-digest:
      enabled: false           # prod 에서만 켬
      cron: "0 0 9 * * *"
```

`application-prod.yml`:

```yaml
paceleague:
  fcm:
    service-account-json: ${FCM_SERVICE_ACCOUNT_JSON}   # 서비스 계정 키 (JSON 문자열 or 파일 경로)
    project-id: ${FCM_PROJECT_ID}
    daily-digest:
      enabled: true
```

- **서비스 계정 키**는 `JWT_SECRET` 과 동일하게 **env var 로만** 주입. yml/git 에 절대 넣지 않음.
  - 키가 길어서 env 로 JSON 통째는 관리가 번거로움 → EC2 파일 배치(`/etc/paceleague/fcm-sa.json`, 앱 컨테이너에 read-only 마운트) 후 경로만 env 로 넘기는 방식 권장.
  - `docs/infra.md` 에 배치 위치·권한·재발급 절차 기록.
- `FirebaseConfig` 는 `@ConditionalOnProperty("paceleague.fcm.service-account-json")` 로 감싸서
  키 없는 로컬에서 앱이 안 뜨는 일 방지.

`build.gradle`:

```gradle
implementation 'com.google.firebase:firebase-admin:9.4.1'   // 최신 버전은 Maven Central 확인 후 고정
```

---

## 10. 동시성 / 멀티 인스턴스

- 현재 단일 인스턴스. `push_send_log` 의 `(kind, target_date)` UNIQUE 가 1차 가드.
- 스케일아웃 시:
  - `@Scheduled` 는 모든 인스턴스에서 발화 → 2대면 동시에 run()
  - UNIQUE INSERT 경쟁에서 진 쪽은 즉시 return → **논리적으로는 1회만 발송**되지만,
    INSERT 시점과 실제 send() 사이에 크래시하면 애매해짐
  - 정석은 **ShedLock** 도입 (`net.javacrumbs.shedlock`, JDBC 락 테이블). `GpsSessionSweeper` 도 같은 숙제라 같이 처리 고려.
- MVP 단계에서는 UNIQUE 가드로 충분하다고 보고 진행, 스케일아웃 계획 시 ShedLock 티켓.

---

## 11. 테스트 (JUnit 5 + Mockito, 성공/실패 케이스)

| 대상 | 케이스 |
|---|---|
| `DailyTerritoryDigestService` | 집계 결과로 문구 조립 정확 / 0건이면 skip / 이미 발송된 날이면 skip / 발송 실패 시 로그 FAILED |
| 타임존 window 계산 | "KST 어제" → `create_at` 저장 TZ 범위 변환이 DST 없는 KST 기준으로 정확 (경계값: 어제 00:00:00, 오늘 00:00:00) |
| `CountTerritoryCapturesPort` 쿼리 | (통합 or 슬라이스) 경계 밖 행 제외, `DISTINCT owner` 정확 |
| `FcmPushAdapter` | `FirebaseMessaging` mock — 토픽/페이로드 조립 검증, 예외 전파 |

CI 는 `build -x test` 라 이 테스트가 배포를 막지는 않지만 규칙상 작성.

---

## 12. 로깅 / 관측

- `log.info("일간 땅따먹기 다이제스트: 대상일={} 땅={} 유저={} status={} messageId={}", ...)`
- `push_send_log` 자체가 감사 로그. 운영에서 `SELECT * FROM push_send_log ORDER BY sno DESC LIMIT 30`.
- 실패율이 눈에 띄면 Firebase 콘솔의 발송 통계와 대조.

---

## 13. 구현 순서 (서버)

1. **선행 확인**: 운영 DB에서 `territory.create_at` 저장 TZ 확정 (§5)
2. `build.gradle` firebase-admin 의존성
3. `notification` 도메인 골격 + `FcmProperties` / `FirebaseConfig` (키 없으면 비활성)
4. 마이그레이션 `push_send_log` + 엔티티/어댑터
5. `TerritoryJpaRepository` 집계 쿼리 + `CountTerritoryCapturesPort` (territory 도메인)
6. `DailyTerritoryDigestService` + window 계산 유틸 + 테스트
7. `FcmPushAdapter` (토픽 발송) + 테스트
8. `DailyTerritoryDigestScheduler` (기본 OFF)
9. `PaceleagueApplication` `@EnableConfigurationProperties` 에 `FcmProperties` 추가
10. `docs/domains.md`(새 도메인·스케줄 잡), `docs/infra.md`(FCM 키 배치), `docs/api.md`(phase 2 시 토큰 API), `CLAUDE.md`(스케줄 잡이 2개가 됨) 갱신
11. 로컬에서 크론을 `*/2 * * * * *` 등으로 바꿔 스모크 → 실제 FCM 테스트 토픽으로 확인
12. prod 배포 후 `application-prod.yml` 로 `enabled: true`, 다음날 09:00 결과 확인

## 14. 앱 팀 의존성 (서버 진행과 병행 필요)

- Firebase 프로젝트에 Android/iOS 앱 등록, `google-services.json` / `GoogleService-Info.plist`
- FCM SDK 연동, **`all` 토픽 구독** (로그인/최초 실행 시), 알림 끄기 시 구독 해제
- 알림 권한: Android 13+ `POST_NOTIFICATIONS` 런타임 요청, iOS 권한 요청
- 알림 탭 → 랜드잇 화면 딥링크 처리, `data.deeplink` 규약 합의
- 서버는 앱이 토픽 구독을 끝내야 실제 도달됨 — **앱 배포가 선행되어야 첫 발송 의미 있음**

## 15. 열린 결정사항

- "몇 개의 땅" 을 territory 행 수로 할지 hex 칸 수(`SUM(hex_count)`)로 할지 — 문구와 함께 확정
- 0건인 날 발송 여부
- phase 2(토큰 테이블 + 개인 알림)를 언제 시작할지 — "랭킹 밀림 알림" 같은 후속 기획과 묶어서
- ShedLock 도입 시점 (멀티 인스턴스 계획과 연동)
