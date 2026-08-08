# AGENTS.md

## 개발 원칙

* Java만 사용한다.
* Kotlin 사용 금지.
* Maven 사용 금지.
* Gradle만 사용한다.
* 과도한 추상화 금지.
* 불필요한 디자인 패턴 사용 금지.
* 읽기 쉬운 코드 우선.
* 혼자 유지보수 가능한 수준으로 구현한다.

---

## 아키텍처 규칙

Layered Architecture를 유지한다.

각 도메인은 아래 구조를 기본으로 사용한다.

```text
controller
service
repository
entity
dto
```

Controller에는 비즈니스 로직을 작성하지 않는다.

비즈니스 로직은 Service에서 처리한다.

DB 접근은 Repository에서만 수행한다.

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

* 대규모 리팩토링 임의 진행 금지.
* 사용하지 않는 기술 추가 금지.
* 프로젝트 전체 구조 변경 금지.
* 기존 인증 구조 임의 변경 금지.
* 불필요한 멀티모듈화 금지.

---

## 코드 스타일 (클린 코드)

* 메서드는 짧고 명확하게 작성.
* 의미 있는 변수명 사용.
* Optional 과도하게 남발 금지.
* Stream 과도하게 남발 금지.
* 유지보수성과 가독성을 우선한다.
* 같은 클래스 안에서 2회 이상 반복되는 로직은 private 메서드로 추출한다. 다만 이를 이유로 새 클래스/인터페이스를 만들지는 않는다 (`과도한 추상화 금지` 규칙 우선).
* 여러 계산/처리 단계가 한 메서드에 섞여 있으면, 각 단계를 이름이 있는 private 메서드로 분리해 메서드 이름이 곧 설명이 되도록 한다.
* 이미 의도적으로 반복되는 패턴(예: 인증 컨트롤러마다 있는 `uno(authentication)` 헬퍼)은 중복이 아니라 스타일이므로 임의로 공통화하지 않는다 — 자세한 배경은 `docs/architecture.md`의 "클린 코드 원칙" 참고.
