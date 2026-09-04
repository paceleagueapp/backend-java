# PaceLeague

Spring Boot 기반 러닝 기록 및 랭킹 서비스입니다.

사용자의 러닝 기록을 저장하고, 기록 기반 점수 계산 및 시즌 랭킹 기능을 제공합니다.

단순 CRUD를 넘어 JWT 인증, Redis 토큰 관리, Docker 배포, AWS 운영 경험까지 직접 구현하는 것을 목표로 개발 중인 프로젝트입니다.

---

# 주요 기능

* 회원가입 / 로그인, JWT 기반 인증·인가 (access token: JWT / refresh token: Redis opaque 문자열)
* 러닝 기록 저장·조회, 서버측 페이스·칼로리·점수 계산
* GPS 좌표 청크 실시간 수집(5분 단위) 및 러닝 종료 시 기록 확정
* 시즌 관리·티어 산정, 개인 점수/티어 조회 + 시즌 리더보드
* 커뮤니티(보드/게시글/댓글/추천) — 서버측 HTML sanitize, 게시글 다국어 번역
* 게시글 이미지/동영상 첨부 — S3 presigned 업로드 + Rekognition 자동 모더레이션
* 러닝 땅따먹기(Territory) — 닫힌 GPS 루프를 "땅"으로, 겹친 러닝으로 공격/점령
* 크루(길드) — 생성/검색/초대/가입 승인/크루원 관리, 크루 랭킹·배지
* 정적 UI 라벨 10개 언어 다국어 (보드명/티어명)
* REST API + Swagger 문서(로컬 전용), Redis 토큰 관리

---

# 기술 스택

## Backend

* Java 21
* Spring Boot 3.5.x
* Spring Security (인증 메커니즘 용도)
* Spring Data JPA / Hibernate
* JWT (jjwt)
* JTS (러닝 땅따먹기 폴리곤 연산)

## Database

* MySQL

## Cache

* Redis

## Infra

* Docker
* AWS EC2 / ECR
* AWS S3 + Rekognition (게시글 미디어 첨부·모더레이션)
* AWS Translate (게시글/댓글 번역)
* Nginx

## CI/CD

* GitHub Actions

---

# 아키텍처

**클린 아키텍처(유스케이스 + 포트 & 어댑터)** 를 적용했습니다. 2026-08-10 사용자 요청으로 기존 Layered 구조(`controller/service/repository`)에서 전환했으며, 멀티모듈 분리 없이 단일 `api` 모듈 안에서 패키지로만 계층을 나눕니다. 상세 근거와 규칙은 [docs/architecture.md](./docs/architecture.md) 참고.

각 도메인 패키지는 아래 구조를 따릅니다.

```text
{domain}/
  domain/{entity, policy, enums}   순수 도메인 — JPA 엔티티를 도메인 모델로 직접 사용, policy는 순수 비즈니스 규칙
  application/
    port/in                        유스케이스 인터페이스
    port/out                       리포지토리 형태의 출력 포트
    service                        유스케이스 구현체 (@Transactional, port/out에만 의존)
    dto                            경계 타입
  adapter/
    in/web                         컨트롤러 (port/in에만 의존, 로직 없음)
    out/persistence                Spring Data JpaRepository + port/out 구현 어댑터
```

요청 흐름:

```text
Client → Nginx → Spring Boot (DispatcherServlet)
  → JwtAuthenticationFilter (토큰 검증, SecurityContext 설정)
  → Controller (adapter/in/web)
  → UseCase 구현체 (application/service)
  → PersistenceAdapter (adapter/out/persistence)
  → MySQL / Redis
```

도메인 간 접근은 포트로만 넘나듭니다 (예: `record` → `season.GetCurrentSeasonPort` / `rank.ApplyScoreUseCase`).

---

# 배포 흐름

`main` 브랜치 push 시 `api/`와 `web/`이 한 번의 파이프라인으로 함께 배포됩니다. 상세는 [docs/setup.md](./docs/setup.md) · [docs/infra.md](./docs/infra.md) 참고.

```text
GitHub push (main)
  → GitHub Actions
      · 저장소 루트에서 Docker 이미지 빌드 (api jar + web/ 를 /web-dist 로 함께 포함)
      · AWS ECR push
      · AWS SSM (AWS-RunShellScript) 로 EC2 에 배포 명령 전달
  → EC2 (.github/ssm-commands.json)
      1. docker pull → 앱 컨테이너 먼저 재시작 (api.paceleague.co.kr 우선)
      2. 이미지에서 /web-dist 추출 → /var/www/paceleague 교체 (실패 시 기존 정적 사이트 유지)
```

---

# 프로젝트 구조

모노레포입니다. 백엔드(`api/`)와 정적 랜딩·커뮤니티 사이트(`web/`)를 함께 관리하고 한 번에 배포합니다. 자세한 내용은 [docs/](./docs/README.md) 참고.

```text
.
├── api/            Spring Boot 백엔드 (api.paceleague.co.kr)
│   └── src/main/java/com/example/paceleague
│       ├── member       회원 가입/로그인/토큰
│       ├── record       러닝 기록 저장·조회, GPS 청크 수집
│       ├── rank         내 점수/티어 조회
│       ├── ranking      리더보드 조회
│       ├── season       시즌 정보 (컨트롤러 없음, 포트만 노출)
│       ├── appversion   모바일 강제/선택 업데이트 체크
│       ├── board        커뮤니티 (보드/게시글/댓글/추천)
│       ├── media        게시글 미디어 첨부 + 모더레이션
│       ├── territory    러닝 땅따먹기
│       ├── crew         크루(길드)
│       └── common       횡단 관심사 (설정/응답/에러/보안/i18n)
├── web/            정적 랜딩 + 커뮤니티 피드 사이트 (paceleague.co.kr)
└── docs/           상세 문서
```

각 도메인 하위는 위 "아키텍처"의 `domain` / `application` / `adapter` 계층 구조를 동일하게 따릅니다.

---

# 실행 방법

## 로컬 실행

```bash
cd api
./gradlew bootRun
```

Windows:

```bash
cd api
gradlew.bat bootRun
```

---

# 테스트 실행

```bash
cd api
./gradlew test
```

---

# 빌드

```bash
cd api
./gradlew clean build
```

---

# Docker 실행

빌드 컨텍스트가 `api/`와 `web/`을 모두 포함하므로 **저장소 루트에서** 실행합니다.

```bash
docker build -t paceleague .
docker run -d -p 8080:8080 --name paceleague paceleague
```

---

# 환경 변수 예시

```text
DB_URL=jdbc:mysql://localhost:3306/paceleague
DB_USERNAME=root
DB_PASSWORD=password

JWT_SECRET=your-secret-key

REDIS_HOST=localhost
REDIS_PORT=6379
```

---

# 보안 주의사항

다음 정보는 Git에 커밋하지 않습니다.

* DB 비밀번호
* JWT Secret
* AWS Access Key
* AWS Secret Key
* 운영용 application.yml
* .env 파일

---

# License

Personal Portfolio Project
