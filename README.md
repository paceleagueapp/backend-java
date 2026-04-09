# 🏃 PaceLeague

Spring Boot 기반으로 개발되었으며, Docker와 AWS(ECR, EC2)를 활용한 **컨테이너 배포 환경**을 구축했습니다.

---

# 🚀 프로젝트 소개

단순 CRUD를 넘어, **이벤트 기반 처리 및 컨테이너 배포 구조**를 직접 설계하고 구현하는 데 중점을 두었습니다.

---

# ✨ 주요 기능

* 🏃 러닝 기록 저장 (시간, 거리)
* 📊 점수 계산 및 티어 산정
* 🔐 JWT 기반 인증/인가
* 🏆 사용자 랭킹 시스템
* 📡 REST API 제공 (Swagger 문서 지원)

---

# 🛠 기술 스택

## Backend

* Java 21
* Spring Boot
* Spring Security (JWT)

## Database

* MySQL (AWS RDS)

## Cache / Messaging

* Redis (Valkey 기반)

## Infra

* AWS EC2
* AWS ECR
* Docker

## CI/CD

* GitHub Actions

---

# 🧱 아키텍처

```
GitHub → GitHub Actions → Docker Build → ECR Push → EC2 Pull → Container Run
```

* GitHub Actions에서 애플리케이션 빌드 및 Docker 이미지 생성
* AWS ECR에 이미지 저장
* EC2에서 최신 이미지를 pull 후 컨테이너 실행

---

# ⚙️ 배포 과정

1. `git push` 발생
2. GitHub Actions 실행
3. Gradle 빌드 후 Docker 이미지 생성
4. AWS ECR에 이미지 push
5. EC2에서 이미지 pull
6. 컨테이너 재시작으로 배포 완료

---

# ▶️ 실행 방법

## Docker 실행

```bash
docker run -d \
  --name paceleague \
  --env-file /opt/paceleague/.env \
  -p 8080:8080 \
  paceleague
```

---

# 🔐 환경 변수

```env
SPRING_PROFILES_ACTIVE=prod

SPRING_DATASOURCE_URL=...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...

SPRING_DATA_REDIS_HOST=...
SPRING_DATA_REDIS_PORT=6379

JWT_SECRET=...
```

※ 민감 정보는 Git에 포함하지 않고 `.env` 파일로 관리

---

# 📄 API 문서

Swagger UI:

```text
http://EC2_PUBLIC_IP:8080/swagger-ui/index.html
```

---

# 📌 향후 개선 사항

* 🔄 무중단 배포 (Blue-Green / Rolling)
* 🌐 Nginx Reverse Proxy 적용 (80/443)
* 🔐 HTTPS 적용 (Let's Encrypt)
* 🚀 ECS 또는 Kubernetes 전환
* 📊 모니터링 (CloudWatch, Prometheus)

---

# 👨‍💻 개발자

Backend Developer

* Java / Spring Boot 기반 서비스 개발
* AWS 및 Docker 기반 인프라 구축 경험 
