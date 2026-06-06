# PaceLeague

Spring Boot 기반 러닝 기록 및 랭킹 서비스입니다.

사용자의 러닝 기록을 저장하고, 기록 기반 점수 계산 및 시즌 랭킹 기능을 제공합니다.

단순 CRUD를 넘어 JWT 인증, Redis 토큰 관리, Docker 배포, AWS 운영 경험까지 직접 구현하는 것을 목표로 개발 중인 프로젝트입니다.

---

# 주요 기능

* 회원가입 / 로그인
* JWT 기반 인증 및 인가
* 러닝 기록 저장
* 거리 및 시간 관리
* 점수 계산
* 시즌 관리
* 티어 산정
* 사용자 랭킹
* REST API 제공
* Swagger API 문서
* Redis 기반 토큰 관리

---

# 기술 스택

## Backend

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA
* QueryDSL
* JWT

## Database

* MySQL

## Cache

* Redis

## Infra

* Docker
* AWS EC2
* AWS ECR
* Nginx

## CI/CD

* GitHub Actions

---

# 아키텍처

```text
Client
  ↓
Nginx
  ↓
Spring Boot Backend
  ↓
MySQL / Redis
```

---

# 배포 흐름

```text
GitHub
  ↓
GitHub Actions
  ↓
Docker Build
  ↓
AWS ECR Push
  ↓
EC2 Pull
  ↓
Docker Run
```

---

# 프로젝트 구조

```text
src/main/java/com/example/paceleague
├── auth
├── user
├── record
├── season
├── ranking
├── common
└── config
```

---

# 실행 방법

## 로컬 실행

```bash
./gradlew bootRun
```

Windows:

```bash
gradlew.bat bootRun
```

---

# 테스트 실행

```bash
./gradlew test
```

---

# 빌드

```bash
./gradlew clean build
```

---

# Docker 실행

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
