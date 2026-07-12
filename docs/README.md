# PaceLeague 문서

PaceLeague 백엔드(Spring Boot 기반 러닝 기록/랭킹 서비스)의 상세 문서 모음입니다.

## 목차

| 문서 | 내용 |
|---|---|
| [architecture.md](./architecture.md) | 전체 아키텍처, 도메인 패키지 구조, 기술 스택, 요청 처리 흐름 |
| [api.md](./api.md) | 전체 REST API 명세 (엔드포인트, 요청/응답, 인증 여부, 에러) |
| [auth.md](./auth.md) | JWT 인증/인가 흐름, 토큰 발급·재발급·폐기 상세 |
| [domains.md](./domains.md) | 도메인별 핵심 비즈니스 로직 (점수 계산, 티어, 랭킹 산정, 버전 체크) |
| [database.md](./database.md) | 엔티티/테이블 구조 및 관계 |
| [setup.md](./setup.md) | 로컬 실행, 환경 변수, Docker, 배포(CI/CD) 가이드 |

## 프로젝트 한 줄 요약

사용자가 러닝 기록을 저장하면 서버가 페이스/칼로리/점수를 계산하고, 시즌 단위로 랭킹을 매기는 Spring Boot 백엔드.

- 인증: JWT (access token: JWT / refresh token: Redis에 저장되는 opaque 랜덤 문자열)
- DB: MySQL (Spring Data JPA)
- 캐시/세션: Redis (refresh token 저장용)
- 배포: GitHub Actions → Docker → AWS ECR → EC2 (SSM)

프로젝트 코딩 규칙은 저장소 루트의 `AGENTS.md`를 따릅니다 (Java/Gradle only, 과도한 추상화 금지, Controller에 비즈니스 로직/트랜잭션 금지 등).
