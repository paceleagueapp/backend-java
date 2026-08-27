# AGENTS.md

## 개발 원칙

* Java만 사용한다.
* Kotlin 사용 금지.
* Maven 사용 금지.
* Gradle만 사용한다.
* 과도한 추상화 금지 — 단, 아래 "아키텍처 규칙"에 명시된 유스케이스/포트&어댑터 구조는 2026-08-10 사용자의 명시적 요청으로 예외적으로 승인되어 현재의 기본 구조다. 이 구조를 벗어나는 추가 추상화(예: 유스케이스를 메서드 단위로 더 잘게 쪼개기, 엔티티를 순수 도메인 객체와 JPA 영속 모델로 이중화하기)는 여전히 금지 — 아래 구조가 이미 "이 프로젝트가 승인한 최대치"다.
* 불필요한 디자인 패턴 사용 금지 (위와 동일한 예외 적용).
* 읽기 쉬운 코드 우선.
* 혼자 유지보수 가능한 수준으로 구현한다.

### ⛔ 배포 금지 (사용자 명시 지시 없이는 절대)

`main` 브랜치에 `git push` 하면 **즉시 운영 배포**된다(GitHub Actions → ECR → SSM → `api.paceleague.co.kr` / `paceleague.co.kr` 반영). 따라서:

* 사용자가 그 메시지에서 **"커밋 푸시" / "푸시해" / "배포해"** 라고 명시하기 전에는 **절대 `git push` 하지 않는다.** "구현해 / 재구현 / 고쳐 / 만들어줘 / 직접 적용해" 는 배포 지시가 아니다 — 코드 작성 + 테스트 + **로컬 커밋까지만** 하고 멈춘다.
* SSM 재배포, 이미지 수동 교체, 워크플로 수동 트리거 등 **다른 경로 배포도 명시 지시 없이는 절대 안 한다.**
* 작업 중 버그를 발견하면 로컬에서 고치고 보고만 한다. 배포 시점은 사용자가 정한다.
* "직접 적용해" 로 DB 마이그레이션을 DB에 적용하는 것은 허용되지만, 그것이 코드 푸시를 승인하는 것은 아니다.
* 사용자가 푸시를 지시한 뒤에는: 푸시하고 멈춘다 — 별도 요청 없이 CI 폴링/운영 curl 하지 않는다.

---

## 아키텍처 규칙

클린 아키텍처(유스케이스 + 포트&어댑터)를 유지한다 — 2026-08-10 이전에는 단순 Layered Architecture(`controller/service/repository/entity/dto`)였으나, 사용자 요청으로 전환됨. 멀티모듈 Gradle 분리는 여전히 하지 않는다(단일 `api` 모듈 내 패키지 구조로만 계층을 나눔).

각 도메인은 아래 구조를 기본으로 사용한다.

```text
{domain}/
  domain/
    entity/        JPA @Entity 그대로 도메인 모델로 사용 (순수 POJO로 이중화하지 않음)
    policy/ enums/  순수 비즈니스 규칙
  application/
    port/in/        유스케이스 인터페이스 (기존 서비스 인터페이스와 1:1 대응, 도메인당 보통 1~3개)
    port/out/        리포지토리 모양의 출력 포트 — Spring Data/JPA import 금지
    service/         유스케이스 구현체, port/out에만 의존
    dto/             Command/Result 경계 타입
  adapter/
    in/web/          컨트롤러, port/in에만 의존
    out/persistence/ Spring Data JpaRepository(`*JpaRepository`) + port/out을 구현하는 얇은 어댑터(`*PersistenceAdapter`)
```

**포트화 대상은 두 가지뿐이다**: (1) 그 도메인 자신의 JPA 리포지토리, (2) 다른 도메인 저장소로의 실제 크로스 도메인 접근. `StringRedisTemplate`, `TranslateClient`, `PasswordEncoder` 같은 범용 인프라 클라이언트는 포트로 감싸지 않고 유스케이스 구현체에 직접 주입한다 — 이것까지 포트화하면 과도한 추상화다.

`common/` 패키지(설정/에러처리/응답포맷/보안필터)는 여러 도메인이 공유하는 진짜 횡단 관심사이므로 위 구조에 억지로 끼워맞추지 않고 지금 형태(`config/error/response/security/web`)를 유지한다. `JwtTokenProvider`는 `member` 도메인이 아니라 `common/security/jwt/`에 있다 — `JwtAuthenticationFilter`/`SecurityConfig`가 직접 의존하는 진짜 공유 인프라이기 때문(자세한 이유는 `docs/architecture.md` 참고).

Controller(adapter/in/web)에는 비즈니스 로직을 작성하지 않는다.

비즈니스 로직은 유스케이스 구현체(application/service)에서 처리한다.

DB 접근은 어댑터(adapter/out/persistence)에서만 수행한다.

---

## DTO 규칙

* Request/Response는 DTO를 사용한다.
* Entity를 API 응답으로 직접 반환하지 않는다. Entity를 그대로 담는 필드(`List<Entity>` 등)도 금지 — 정적 팩토리 메서드(예: `XxxResponse.from(entity)`)로 값을 옮겨 담는다.
* DTO 필드 타입은 실제 값 타입을 명시한다. 타입이 불확실하다고 `Object`로 남겨두지 않는다.
* Validation 가능한 경우 적극적으로 사용한다.

---

## Transaction 규칙

* 데이터 변경 로직은 `@Transactional` 사용.
* 조회 로직은 `@Transactional(readOnly = true)` 사용.
* Controller에 Transaction 사용 금지.

---

## QueryDSL 규칙

* 복잡한 조회는 QueryDSL 사용.
* 단순 CRUD는 JpaRepository 우선 사용.
* Native Query 남발 금지.

---

## 보안 규칙

* JWT 기반 인증 유지.
* Secret 값 하드코딩 금지.
* 운영용 설정 Git 커밋 금지.
* 인증 필요한 API는 반드시 보호한다.

---

## DB 규칙

* MySQL 사용 기준 유지.
* PK는 가능하면 BIGINT 사용.
* 기존 컬럼명/테이블명 함부로 변경 금지.
* 기존 데이터 삭제 로직 생성 시 주의.

---

## Redis 규칙

Redis는 인증 및 토큰 관리 용도로 사용한다.

예시:

* Refresh Token
* 블랙리스트 토큰
* 세션성 데이터

비즈니스 로직과 Redis 로직은 분리한다.

---

## API 규칙

RESTful 스타일 유지.

HTTP Method 규칙:

* GET : 조회
* POST : 생성
* PUT/PATCH : 수정
* DELETE : 삭제

응답 구조는 일관성 있게 유지한다.

Global Exception Handler 사용을 우선한다.

---

## 테스트 규칙

* JUnit5 사용.
* Mockito 사용.
* 테스트 함수명 한글 사용 가능.
* 성공/실패 케이스 모두 작성 권장.
* 기존 테스트 삭제 금지.

---

## Docker 규칙

현재 Docker 기반 배포 구조 유지.

배포 흐름:

```text
GitHub Actions
→ Docker Build
→ AWS ECR Push
→ EC2 Pull
→ Docker Run
```

특별한 요청 없이는 Docker 구조 변경 금지.

---

## AWS 운영 규칙

현재 운영 기준:

* AWS EC2
* AWS ECR

초기 비용 절감을 우선한다.

명시적 요청 없이는 아래 서비스 추가 금지:

* ECS
* EKS
* RDS
* ElastiCache

---

## 로그 규칙

* 장애 원인 파악 가능한 로그 작성.
* DEBUG 로그 남발 금지.
* 민감 정보 로그 출력 금지.
* 운영 환경 로그 가독성 유지.

---

## 금지 사항

* 대규모 리팩토링 임의 진행 금지 — 2026-08-10의 클린 아키텍처 전환은 사용자의 명시적 요청에 따른 예외였고, 이후에도 사용자 요청 없이 구조를 다시 바꾸는 것은 여전히 금지.
* 사용하지 않는 기술 추가 금지.
* 프로젝트 전체 구조 변경 금지 (위와 동일한 예외 적용, 위 아키텍처 규칙이 현재 승인된 구조).
* 기존 인증 구조 임의 변경 금지 (JWT/Redis refresh token 방식 자체는 이번 전환에서도 그대로 유지됨, 컴포넌트 위치만 재배치).
* 불필요한 멀티모듈화 금지 — 이번 전환도 단일 Gradle 모듈 안에서만 이뤄졌음, 이 규칙은 그대로 유효.

---

## 코드 스타일 (클린 코드)

* 메서드는 짧고 명확하게 작성.
* 의미 있는 변수명 사용.
* Optional 과도하게 남발 금지.
* Stream 과도하게 남발 금지.
* 유지보수성과 가독성을 우선한다.
* 같은 클래스 안에서 2회 이상 반복되는 로직은 private 메서드로 추출한다. 다만 이를 이유로 새 클래스/인터페이스를 만들지는 않는다 (`과도한 추상화 금지` 규칙 우선).
* 여러 계산/처리 단계가 한 메서드에 섞여 있으면, 각 단계를 이름이 있는 private 메서드로 분리해 메서드 이름이 곧 설명이 되도록 한다.
* 인증된 컨트롤러가 현재 로그인한 회원의 PK를 꺼낼 땐 `@MemberSno Long memberSno` 파라미터(`common.web.MemberSnoArgumentResolver`)를 쓴다 — 예전에는 컨트롤러마다 `uno(authentication)` 캐스팅 헬퍼가 반복됐지만, 클린 아키텍처 전환 시 공유 리졸버로 통합됨(자세한 배경은 `docs/architecture.md` 참고). 이 리졸버 외에 새로운 공통 추상화를 임의로 추가하지는 않는다.
