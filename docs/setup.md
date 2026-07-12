# 로컬 실행 / 환경 변수 / 배포

## 로컬 실행 요구사항

- JDK 21 (Gradle toolchain으로 자동 관리됨)
- 실행 중인 MySQL 인스턴스
- 실행 중인 Redis 인스턴스 (기본 `127.0.0.1:6379`)
- Spring 프로필 `local` 활성화

## 환경 변수 (local 프로필 기준, `application-local.yml`)

| 변수 | 설명 |
|---|---|
| `DB_URL` | MySQL JDBC URL (예: `jdbc:mysql://localhost:3306/paceleague`) |
| `DB_USERNAME` | MySQL 사용자명 |
| `DB_PASSWORD` | MySQL 비밀번호 |
| `JWT_SECRET` | JWT 서명 시크릿 (HMAC 키) |

`.env` 파일을 두면 `spring-dotenv` 의존성이 자동으로 로딩합니다. `.env`/운영용 설정 파일은 Git에 커밋하지 않습니다.

local 프로필의 JPA 설정은 `ddl-auto: update`이므로 로컬에서는 엔티티 변경 시 스키마가 자동 반영됩니다(운영은 `validate`로 다름, [database.md](./database.md) 참고).

## 실행 명령

```bash
./gradlew bootRun          # Windows: gradlew.bat bootRun
```

로컬 프로필 지정이 필요하면 `SPRING_PROFILES_ACTIVE=local` 환경 변수를 함께 설정합니다.

## 테스트

```bash
./gradlew test
./gradlew test --tests "com.example.paceleague.SomeTest"   # 단일 클래스만
```

## 빌드

```bash
./gradlew clean build          # 테스트 포함
./gradlew clean build -x test  # 테스트 스킵 (CI/Docker에서 사용하는 방식)
```

## Docker

`Dockerfile`은 2-stage 빌드입니다: `eclipse-temurin:21-jdk`로 `bootJar`를 빌드하고, `eclipse-temurin:21-jre` 런타임 이미지에 jar만 복사해 실행합니다(`-x test`로 테스트는 스킵).

```bash
docker build -t paceleague .
docker run -d -p 8080:8080 --name paceleague paceleague
```

컨테이너 실행 시에도 위 환경 변수(`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`)와 `SPRING_PROFILES_ACTIVE`(local 또는 prod), Redis 접속 정보를 `-e` 옵션이나 오케스트레이션 설정으로 주입해야 합니다.

## 배포 (CI/CD)

`main` 브랜치에 push되면 `.github/workflows/deploy.yml`이 실행됩니다.

```text
GitHub push (main)
  → GitHub Actions: JDK 21 세팅, `./gradlew clean build -x test`
  → AWS 자격증명 설정 (secrets: AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY, region ap-northeast-2)
  → Docker 이미지 빌드 & 태깅
  → AWS ECR로 push
  → AWS SSM (`aws ssm send-command`, document `AWS-RunShellScript`)로 EC2 인스턴스(secret: EC2_INSTANCE_ID)에 배포 명령 전달
     (실제 배포 셸 커맨드는 `.github/ssm-commands.json`에 정의됨)
```

- 배포는 EC2에 직접 SSH하지 않고 AWS Systems Manager(SSM)를 통해 명령을 실행하는 방식입니다.
- 필요한 GitHub Secrets: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `EC2_INSTANCE_ID` (+ EC2 쪽에서 ECR pull 권한 및 `.env`/환경 변수 구성이 사전에 되어 있어야 함, 이 저장소 범위 밖의 설정).
- 인프라는 EC2 + ECR만 사용하는 것이 원칙이며(`AGENTS.md`), 명시적 요청 없이 ECS/EKS/RDS/ElastiCache 등을 추가하지 않는 것이 프로젝트 규칙입니다.

## Swagger / API 문서 UI

애플리케이션 실행 후:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI 스펙(JSON): `http://localhost:8080/v3/api-docs`

`OpenApiConfig`에서 `bearerAuth`(HTTP Bearer, JWT) 보안 스키마가 전역으로 등록되어 있어, Swagger UI의 "Authorize" 버튼에 access token만 넣으면 인증이 필요한 API도 바로 테스트할 수 있습니다.
