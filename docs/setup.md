# 로컬 실행 / 환경 변수 / 배포

이 저장소는 모노레포입니다: `api/`(Spring Boot 백엔드)와 `web/`(정적 랜딩/약관 페이지)가 한 저장소, 한 배포 파이프라인으로 관리됩니다. 이 문서는 `api/` 기준 로컬 실행/빌드/배포를 다룹니다. 실제 운영 서버의 도메인 라우팅(Nginx) 구조는 [infra.md](./infra.md)를 참고하세요.

## 로컬 실행 요구사항

- JDK 21 (Gradle toolchain으로 자동 관리됨)
- 실행 중인 MySQL 인스턴스
- 실행 중인 Redis 인스턴스 (기본 `127.0.0.1:6379`)
- Spring 프로필 `local` 활성화

## 환경 변수 (local 프로필 기준, `api/src/main/resources/application-local.yml`)

| 변수 | 설명 |
|---|---|
| `DB_URL` | MySQL JDBC URL (예: `jdbc:mysql://localhost:3306/paceleague`) |
| `DB_USERNAME` | MySQL 사용자명 |
| `DB_PASSWORD` | MySQL 비밀번호 |
| `JWT_SECRET` | JWT 서명 시크릿 (HMAC 키) |

`api/.env` 파일을 두면 `spring-dotenv` 의존성이 자동으로 로딩합니다 (`./gradlew`를 `api/`에서 실행하므로 `.env`도 그 위치에 있어야 함). `.env`/운영용 설정 파일은 Git에 커밋하지 않습니다.

local 프로필의 JPA 설정은 `ddl-auto: update`이므로 로컬에서는 엔티티 변경 시 스키마가 자동 반영됩니다(운영은 `validate`로 다름, [database.md](./database.md) 참고).

## 실행 명령

모든 Gradle 명령은 `api/` 디렉터리에서 실행합니다.

```bash
cd api
./gradlew bootRun          # Windows: gradlew.bat bootRun
```

로컬 프로필 지정이 필요하면 `SPRING_PROFILES_ACTIVE=local` 환경 변수를 함께 설정합니다.

## 테스트

```bash
cd api
./gradlew test
./gradlew test --tests "com.example.paceleague.SomeTest"   # 단일 클래스만
```

## 빌드

```bash
cd api
./gradlew clean build          # 테스트 포함
./gradlew clean build -x test  # 테스트 스킵 (CI/Docker에서 사용하는 방식)
```

## web/ (정적 사이트)

빌드 단계가 없는 순수 정적 HTML입니다. `paceleague.co.kr` / `www.paceleague.co.kr`에서 서빙되며, 로컬에서 미리 보려면 그냥 브라우저로 `web/index.html`을 열거나 아무 정적 서버(`npx serve web` 등)로 띄우면 됩니다. Java 앱과는 런타임에서 완전히 분리되어 있습니다 — Spring Boot가 이 파일들을 서빙하지 않습니다.

## Docker

`Dockerfile`은 **저장소 루트에서** 빌드합니다(빌드 컨텍스트가 `api/`와 `web/` 둘 다 필요하기 때문). 2-stage 빌드: `eclipse-temurin:21-jdk`로 `api/`의 `bootJar`를 빌드하고, `eclipse-temurin:21-jre` 런타임 이미지에 jar와 `web/`(`/web-dist` 경로)을 함께 담습니다. `/web-dist`는 앱이 서빙하지 않고, 배포 시 이미지에서 꺼내 EC2의 Nginx 웹루트로 옮기는 용도입니다 ([infra.md](./infra.md) 참고).

```bash
docker build -t paceleague .          # 저장소 루트에서 실행
docker run -d -p 8080:8080 --name paceleague paceleague
```

컨테이너 실행 시에도 위 환경 변수(`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`)와 `SPRING_PROFILES_ACTIVE`(local 또는 prod), Redis 접속 정보를 `-e` 옵션이나 오케스트레이션 설정으로 주입해야 합니다.

## 배포 (CI/CD)

`main` 브랜치에 push되면 `.github/workflows/deploy.yml`이 실행되고, **`api/`와 `web/`이 한 번에 함께 배포**됩니다.

```text
GitHub push (main)
  → GitHub Actions: JDK 21 세팅, api/ 에서 `./gradlew clean build -x test`
  → AWS 자격증명 설정 (secrets: AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY, region ap-northeast-2)
  → Docker 이미지 빌드(저장소 루트, api+web 모두 포함) & 태깅
  → AWS ECR로 push
  → AWS SSM (`aws ssm send-command`, document `AWS-RunShellScript`)로 EC2 인스턴스(secret: EC2_INSTANCE_ID)에 배포 명령 전달
     (`.github/ssm-commands.json`이 이미지 pull → web 정적 파일을 /var/www/paceleague로 교체 → 앱 컨테이너 재시작까지 순서대로 수행)
```

- 배포는 EC2에 직접 SSH하지 않고 AWS Systems Manager(SSM)를 통해 명령을 실행하는 방식입니다.
- 필요한 GitHub Secrets: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `EC2_INSTANCE_ID` (+ EC2 쪽에서 ECR pull 권한 및 `.env`/환경 변수 구성이 사전에 되어 있어야 함, 이 저장소 범위 밖의 설정).
- 인프라는 EC2 + ECR만 사용하는 것이 원칙이며(`AGENTS.md`), 명시적 요청 없이 ECS/EKS/RDS/ElastiCache/S3 등을 추가하지 않는 것이 프로젝트 규칙입니다. `web/` 배포도 이 원칙에 따라 별도 스토리지(S3 등) 없이 기존 Docker 이미지에 실어 나르는 방식으로 구현했습니다.
- 실제 도메인별 라우팅(어느 도메인이 앱으로, 어느 도메인이 정적 사이트로 연결되는지)은 [infra.md](./infra.md)에서 다룹니다.

## Swagger / API 문서 UI

로컬 실행 후:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI 스펙(JSON): `http://localhost:8080/v3/api-docs`

**운영에서는 비활성화되어 있습니다** — `application-prod.yml`에서 `springdoc.api-docs.enabled` / `springdoc.swagger-ui.enabled`를 `false`로 설정해 `https://api.paceleague.co.kr/swagger-ui.html`, `/v3/api-docs`가 모두 404입니다. API 문서는 이 저장소의 [api.md](./api.md)를 기준으로 삼습니다.

`OpenApiConfig`는 `bearerAuth`(HTTP Bearer, JWT) 스킴을 정의만 하고, 실제 인증이 필요한 컨트롤러(Record/Rank/Ranking)에만 `@SecurityRequirement`로 적용합니다. Member/AppVersion처럼 인증이 필요 없는 API는 Swagger UI에서도 자물쇠 아이콘 없이 표시됩니다. 인증이 필요한 API를 테스트하려면 Swagger UI 우측 상단 **Authorize** 버튼에 `POST /api/member/login`(또는 `/join`)으로 발급받은 `accessToken`을 넣으면 됩니다.
