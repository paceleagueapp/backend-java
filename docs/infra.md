# 인프라 / 도메인 구성

`docs/setup.md`가 빌드·로컬 실행·CI 파이프라인을 다룬다면, 이 문서는 **실제 운영 서버(EC2)에서 도메인이 어떻게 연결되어 있는지**를 다룹니다. AWS 콘솔/EC2에 직접 접속하지 않아도 이 문서만으로 운영 구조를 파악할 수 있도록 정리했습니다.

## 서버 구성 (EC2 2대)

| 인스턴스 이름 | 역할 |
|---|---|
| `paceleague` | Java 앱(Docker 컨테이너) + Nginx. 이 저장소가 배포되는 대상. |
| `paceleague-db` | MariaDB + Redis. 로컬 `.env`(`api/.env`, gitignored)의 `DB_URL`/`REDIS_URL`이 이 인스턴스를 가리킴. |

`paceleague` 인스턴스만 SSM 관리 대상(Managed Instance)이며, 배포 파이프라인(`.github/workflows/deploy.yml`)이 이 인스턴스에만 명령을 보냅니다.

## 도메인 라우팅 (Nginx)

`paceleague` EC2의 `/etc/nginx/conf.d/paceleague.conf`에 두 도메인이 완전히 다른 대상으로 라우팅되어 있습니다.

| 도메인 | Nginx 설정 | 실제 서빙 대상 |
|---|---|---|
| `api.paceleague.co.kr` | `proxy_pass http://127.0.0.1:8080;` | Docker 컨테이너로 뜬 이 저장소의 Java 앱 (`api/`) |
| `paceleague.co.kr`, `www.paceleague.co.kr` | `root /var/www/paceleague; index index.html;` | 정적 파일 (이 저장소의 `web/`) |

두 도메인 모두 Let's Encrypt(Certbot)로 SSL이 적용되어 있고(`/etc/letsencrypt/live/...`), HTTP(80)는 HTTPS로 301 리다이렉트됩니다. Nginx 설정 자체(인증서 갱신 등)는 이 저장소가 관리하지 않고 서버에 그대로 둡니다 — 이 저장소가 책임지는 건 **각 경로가 서빙하는 콘텐츠(`api/`, `web/`)** 뿐입니다.

Swagger는 별도 설정 없이 `https://api.paceleague.co.kr/swagger-ui.html`에서 바로 열립니다 (Nginx가 8080 전체를 프록시하기 때문에 앱의 모든 경로가 그대로 노출됨).

Nginx가 TLS를 종료하고 `http://127.0.0.1:8080`으로 평문 프록시하므로, 앱이 `X-Forwarded-Proto` 등 forwarded 헤더를 신뢰하도록 `server.forward-headers-strategy: framework`를 `application.yml`에 설정해뒀습니다. 이게 없으면 Springdoc이 OpenAPI `servers` URL의 scheme을 요청 그대로(`http`)로 오인해 생성하고, Swagger UI의 "Try it out"이 `http://api.paceleague.co.kr`로 요청을 보내면서 scheme 불일치로 브라우저가 이를 cross-origin(preflight)으로 취급 — `/api/ranking/top10` 외에는 CORS 설정이 없어 403 "Invalid CORS request"로 막히는 문제가 있었습니다(2026-07-12에 확인 및 수정).

### 검색엔진 크롤링 차단 (`api.paceleague.co.kr`)

`api.paceleague.co.kr`은 API 전용 도메인이라 검색엔진에 노출될 필요가 없습니다. `api/src/main/resources/static/robots.txt`(Spring Boot 기본 정적 리소스 서빙)로 전체 `Disallow: /`를 응답하며, `SecurityConfig`의 공개 경로 목록에 `/robots.txt`를 추가해 인증 없이 200으로 받을 수 있게 했습니다 — robots.txt가 401/403 등 4xx로 응답되면 대부분의 크롤러가 "제약 없음"으로 해석해 오히려 전체 크롤링을 허용해버리기 때문에, 반드시 인증 예외 목록에 넣어야 의도대로 동작합니다.

`paceleague.co.kr`(정적 사이트, `web/`)에는 별도 robots.txt가 없어 기본값(크롤링 허용)이며, 이미 `sitemap.xml`을 두고 있으므로 의도적으로 인덱싱 대상입니다. 이 차단은 `api.paceleague.co.kr`에만 적용됩니다.

## `web/` 파일 출처

`web/` 폴더는 2026-07-12에 `paceleague` EC2의 `/var/www/paceleague`에 있던 파일을 그대로 가져와 이 저장소에 편입한 것입니다.

- `index.html` — 앱 소개 랜딩 페이지 (Google Play 링크 포함)
- `ko/`, `en/` — 앱스토어 심사에 필요한 이용약관/개인정보처리방침/계정삭제 안내 (한/영)
- `app-ads.txt` — AdMob 광고 검증 파일
- `sitemap.xml` — 실제 사용 중인 사이트맵
- `ko/privacy_bak.html` — `privacy.html`의 백업본으로 보이며 실제 서비스 경로에서는 쓰이지 않음.

> `t_sitemap.xml`(다른 도메인 `onedaykorea.co.kr` 내용이 들어있던 관련 없는 잔여 파일)은 2026-07-12에 삭제했습니다.

## 배포 시 서버에 반영되는 방식

기존에는 이 두 도메인의 콘텐츠가 각각 다른 경로로 관리되고 있었습니다: Java 앱은 GitHub Actions → ECR → SSM으로 자동 배포됐지만, `web/`에 해당하는 정적 파일은 서버에 직접 올려져 있어 **git으로 버전 관리되지 않는 상태**였습니다.

이제는 `api/`와 `web/`을 하나의 Docker 이미지에 함께 담아 배포합니다 (자세한 메커니즘은 `CLAUDE.md`의 Deploy 섹션 참고). 요약하면:

1. `Dockerfile`이 `api/`로 jar를 빌드하고, 최종 이미지에 `web/`도 `/web-dist` 경로로 함께 담습니다.
2. GitHub Actions가 이 이미지를 ECR에 push합니다.
3. `.github/ssm-commands.json`이 EC2에서 이미지를 pull한 뒤 **앱 컨테이너부터 먼저 재시작**하고(`api.paceleague.co.kr`가 오래 끊기지 않는 게 최우선), 그 다음 `/web-dist`를 꺼내 `/var/www/paceleague`로 교체합니다. 정적 파일 추출이 실패해도 기존 `/var/www/paceleague`는 그대로 남도록 가드되어 있습니다.

즉 **`main`에 push 한 번으로 `api.paceleague.co.kr`(Java 앱)과 `paceleague.co.kr`(정적 사이트)이 동시에 갱신**됩니다. 새 AWS 리소스(S3 등)는 추가하지 않고 기존 EC2/ECR 구조 그대로 사용합니다.

## AWS 자격증명 관련 참고

배포용 IAM 사용자(`github-actions-deploy`)는 ECR push, SSM 명령 실행 등 **배포에 필요한 권한으로 좁게 스코프**되어 있습니다. `ec2:DescribeInstances`, `route53:ListHostedZones` 등 조회 권한은 없어서, 이 문서의 인프라 정보는 SSM(`AWS-RunShellScript`)으로 EC2 내부에서 직접 조회해 확인한 것입니다. EC2 인스턴스 목록/Route53 레코드 등을 다시 확인하려면 별도의 조회 권한이 있는 IAM 사용자나 AWS 콘솔이 필요합니다.
