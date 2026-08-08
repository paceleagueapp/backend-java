# PaceLeague 문서

PaceLeague(Spring Boot 기반 러닝 기록/랭킹 서비스)의 상세 문서 모음입니다. 이 저장소는 모노레포로, `api/`(백엔드)와 `web/`(정적 랜딩/약관 사이트)를 함께 관리하고 함께 배포합니다.

## 목차

| 문서 | 내용 |
|---|---|
| [architecture.md](./architecture.md) | 전체 아키텍처, 도메인 패키지 구조, 기술 스택, 요청 처리 흐름 |
| [api.md](./api.md) | 전체 REST API 명세 (엔드포인트, 요청/응답, 인증 여부, 에러) |
| [auth.md](./auth.md) | JWT 인증/인가 흐름, 토큰 발급·재발급·폐기 상세 |
| [domains.md](./domains.md) | 도메인별 핵심 비즈니스 로직 (점수 계산, 티어, 랭킹 산정, 버전 체크) |
| [database.md](./database.md) | 엔티티/테이블 구조 및 관계 |
| [setup.md](./setup.md) | 로컬 실행, 환경 변수, Docker, 배포(CI/CD) 가이드 |
| [infra.md](./infra.md) | 운영 EC2/Nginx 도메인 라우팅 (`api.paceleague.co.kr`, `paceleague.co.kr`이 실제로 어디로 연결되는지) |
| [migrations/](./migrations/) | 수동 DB 마이그레이션 SQL (이 저장소에는 자동 마이그레이션 도구가 없음 — 운영 DB에 직접 실행해야 함, 배포 전 실행 순서는 각 파일 상단 주석 참고) |

## 프로젝트 한 줄 요약

사용자가 러닝 기록을 저장하면 서버가 페이스/칼로리/점수를 계산하고, 시즌 단위로 랭킹을 매기는 Spring Boot 백엔드(`api/`) + 앱 소개/약관 정적 사이트(`web/`).

- 인증: JWT (access token: JWT / refresh token: Redis에 저장되는 opaque 랜덤 문자열)
- DB: MySQL/MariaDB (Spring Data JPA)
- 캐시/세션: Redis (refresh token 저장용)
- 배포: GitHub Actions → Docker(`api/`+`web/` 통합 이미지) → AWS ECR → EC2 (SSM) — 한 번의 push로 `api.paceleague.co.kr`과 `paceleague.co.kr` 둘 다 갱신됨 ([infra.md](./infra.md) 참고)

프로젝트 코딩 규칙은 저장소 루트의 `AGENTS.md`를 따릅니다 (Java/Gradle only, 과도한 추상화 금지, Controller에 비즈니스 로직/트랜잭션 금지 등).
