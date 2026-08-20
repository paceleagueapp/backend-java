# 인프라 / 도메인 구성

`docs/setup.md`가 빌드·로컬 실행·CI 파이프라인을 다룬다면, 이 문서는 **실제 운영 서버(EC2)에서 도메인이 어떻게 연결되어 있는지**를 다룹니다. AWS 콘솔/EC2에 직접 접속하지 않아도 이 문서만으로 운영 구조를 파악할 수 있도록 정리했습니다.

## 서버 구성 (EC2 2대)

| 인스턴스 이름 | 역할 |
|---|---|
| `paceleague` | Java 앱(Docker 컨테이너) + Nginx. 이 저장소가 배포되는 대상. |
| `paceleague-db` | MariaDB + Redis. 로컬 `.env`(`api/.env`, gitignored)의 `DB_URL`/`REDIS_URL`이 이 인스턴스를 가리킴. |

`paceleague` 인스턴스만 SSM 관리 대상(Managed Instance)이며, 배포 파이프라인(`.github/workflows/deploy.yml`)이 이 인스턴스에만 명령을 보냅니다.

### 알려진 보안 이슈: EC2 보안그룹이 필요 이상으로 공인 IP에 노출됨 — 2026-08-10 조치 완료

2026-08-10 보안 점검 중 확인: `paceleague-db`(보안그룹 `sg-0c48f770c3f2ba157`, `launch-wizard-2`)의 3306(MariaDB) 인바운드에 앱 서버(`3.38.33.235/32`)로 제한하는 정상 규칙과 별개로 **`0.0.0.0/0`(전체 공개) 규칙이 중복으로 걸려 있어**, 로컬 PC에서 `api/.env`의 `DB_URL`(`useSSL=false`)로 그대로 직접 접속되는 것을 재현 확인했습니다.

- MariaDB: `useSSL=false`로 평문 전송, 계정/비밀번호가 서비스명 기반의 예측 가능한 패턴(`admin` / `PaceLeague!`) — 사전 대입 공격에 취약.
- ~~Redis도 노출~~ → 재확인 결과 **Redis(6379)는 처음부터 `3.38.33.235/32`(앱 서버)로만 제한되어 있었고 노출된 적이 없었습니다.** 이전 버전 문서에서 잘못 기재했던 부분을 정정합니다.
- DB 자격증명이 `api/.env`(gitignored, 로컬 파일)에 평문으로 저장되어 있어 로컬 PC 유출 시 DB 전체 권한이 그대로 노출되는 점은 별개로 여전히 유효한 리스크.
- SSH(22)도 `0.0.0.0/0`으로 열려있는 게 이번에 추가로 확인됨.

**조치 완료 (3306 + 22)**: `github-actions-deploy` IAM 사용자에 `ec2:DescribeSecurityGroups`/`ec2:AuthorizeSecurityGroupIngress`/`ec2:RevokeSecurityGroupIngress`를 인라인 정책으로 임시 부여받아 아래를 처리:
1. 3306: 중복돼 있던 `0.0.0.0/0` 규칙만 `aws ec2 revoke-security-group-ingress`로 삭제, 앱 서버용 `3.38.33.235/32` 규칙은 유지 — 서비스 영향 없음(`api.paceleague.co.kr` 200 확인), 로컬 PC 재접속 시도는 타임아웃으로 차단됨을 확인.
2. 22: 관리자 현재 접속 IP(`58.228.185.222/32`)로 새 규칙을 먼저 추가하고 접속 가능함을 확인한 뒤, `0.0.0.0/0` 규칙을 삭제(락아웃 방지를 위해 추가→확인→삭제 순서로 진행). 이 IP가 유동 IP라면 바뀔 때마다 콘솔에서 규칙을 갱신해야 함.

**같은 점검에서 앱 서버(`paceleague`, 보안그룹 `sg-0adbde9fc10acc0e1`, `launch-wizard-1`)에서도 추가로 발견 및 조치**:
- 8080(Spring Boot 앱 포트 자체)이 `0.0.0.0/0`으로 열려있어 Nginx/TLS를 완전히 우회해 평문 HTTP로 로그인 요청 등을 직접 보낼 수 있었음 — Nginx는 `127.0.0.1:8080`으로만 내부 프록시하므로 외부 공개될 이유가 없어 규칙 자체를 삭제(루프백 트래픽은 SG를 안 타므로 삭제해도 서비스 영향 없음, 확인 완료).
- 3306도 `0.0.0.0/0`으로 열려있었는데, SSM으로 `ss -tlnp`를 확인해보니 앱 서버엔 3306을 듣는 프로세스가 아예 없어 순수 leftover 규칙이었음 — 삭제.
- 22(SSH)도 `0.0.0.0/0` → `paceleague-db`와 동일하게 관리자 현재 IP(`58.228.185.222/32`)로 제한(추가→확인→삭제 순서로 안전하게 진행).

작업에 쓰인 임시 IAM 인라인 정책은 최소 권한 원칙에 따라 작업 후 회수 대상(콘솔에서 직접 제거 필요 — 이 IAM 사용자 자체가 `iam:*` 권한은 없어 자기 정책을 스스로 지울 수 없음).

### 알려진 보안 이슈: 안 쓰는 `paceleague-valkey-sg`가 전체공개로 방치됨 — 2026-08-10 조치 완료

2026-08-10 점검 중 계정에 `paceleague-valkey-sg`(`sg-0febf12ddd1b5e62b`, 설명 `valkey-sec`)라는, 이 문서 어디에도 언급된 적 없는 보안그룹을 발견 — 6379(Redis/Valkey) 포트가 `0.0.0.0/0`으로 전체 공개되어 있었습니다. `elasticache:Describe*`/`ec2:DescribeNetworkInterfaces`/`ec2:DescribeTags`로 추가 확인한 결과 ElastiCache 캐시 클러스터·복제 그룹 0개, 이 SG를 쓰는 네트워크 인터페이스(ENI)도 0개 — **어떤 리소스에도 붙어있지 않은 leftover 보안그룹**으로 확인됐습니다(실제 Redis는 `paceleague-db` EC2에서 직접 구동 중이며, 이건 과거 ElastiCache로 옮기려다 만 흔적으로 추정). ElastiCache Serverless 캐시까지는 AWS CLI 버전 제약으로 API 조회는 못 했지만, ENI가 0개인 점을 볼 때 실사용 가능성은 낮다고 판단했습니다.

**조치 완료**: 실사용 리소스가 없어 서비스 영향 없이 `0.0.0.0/0` 6379 인바운드 규칙 삭제. SG 자체 삭제는 `ec2:DeleteSecurityGroup` 권한이 없어 규칙만 비웠습니다(원하면 콘솔에서 SG 자체도 삭제 가능).

**아직 미조치로 남은 것**:
1. DB 비밀번호를 강력한 랜덤값으로 교체 (교체 시 EC2에 떠 있는 앱 컨테이너의 `DB_PASSWORD` 환경변수도 함께 갱신 후 재시작 필요 — 별도 조율 필요).
2. 가능하면 자격증명을 AWS Secrets Manager/Parameter Store로 이관(`.env` 평문 대신).
3. MariaDB `useSSL=true` 전환 — **2026-08-10 확인: 현재 MariaDB(10.5.29)는 SSL이 아예 설정되어 있지 않음**(핸드셰이크의 `CLIENT_SSL` capability flag 꺼짐, 앱 서버를 경유해 raw 소켓으로 직접 검증). JDBC URL만 바꾸면 연결 자체가 실패하므로, 서버에 인증서 발급 + `my.cnf`(`ssl_cert`/`ssl_key`/`ssl_ca`) 설정 + MariaDB 재시작이 선행되어야 함.

## 도메인 라우팅 (Nginx)

`paceleague` EC2의 `/etc/nginx/conf.d/paceleague.conf`에 두 도메인이 완전히 다른 대상으로 라우팅되어 있습니다.

| 도메인 | Nginx 설정 | 실제 서빙 대상 |
|---|---|---|
| `api.paceleague.co.kr` | `proxy_pass http://127.0.0.1:8080;` | Docker 컨테이너로 뜬 이 저장소의 Java 앱 (`api/`) |
| `paceleague.co.kr`, `www.paceleague.co.kr` | `root /var/www/paceleague; index index.html;` | 정적 파일 (이 저장소의 `web/`) |

두 도메인 모두 Let's Encrypt(Certbot)로 SSL이 적용되어 있고(`/etc/letsencrypt/live/...`), HTTP(80)는 HTTPS로 301 리다이렉트됩니다. Nginx 설정 자체(인증서 갱신 등)는 이 저장소가 관리하지 않고 서버에 그대로 둡니다 — 이 저장소가 책임지는 건 **각 경로가 서빙하는 콘텐츠(`api/`, `web/`)** 뿐입니다.

Swagger는 별도 설정 없이 `https://api.paceleague.co.kr/swagger-ui.html`에서 바로 열립니다 (Nginx가 8080 전체를 프록시하기 때문에 앱의 모든 경로가 그대로 노출됨).

Nginx가 TLS를 종료하고 `http://127.0.0.1:8080`으로 평문 프록시하므로, 앱이 `X-Forwarded-Proto` 등 forwarded 헤더를 신뢰하도록 `server.forward-headers-strategy: framework`를 `application.yml`에 설정해뒀습니다. 이게 없으면 Springdoc이 OpenAPI `servers` URL의 scheme을 요청 그대로(`http`)로 오인해 생성하고, Swagger UI의 "Try it out"이 `http://api.paceleague.co.kr`로 요청을 보내면서 scheme 불일치로 브라우저가 이를 cross-origin(preflight)으로 취급 — `/api/ranking/top10` 외에는 CORS 설정이 없어 403 "Invalid CORS request"로 막히는 문제가 있었습니다(2026-07-12에 확인 및 수정).

### 알려진 보안 이슈: `paceleague.co.kr`에 보안 헤더 부재 (클릭재킹)

2026-08-10 보안 점검 중 확인: 두 도메인의 실제 응답 헤더를 비교하면 `api.paceleague.co.kr`은 Spring Security 기본값으로 `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Strict-Transport-Security`가 자동으로 붙는 반면, `paceleague.co.kr`(정적 파일을 Nginx가 그대로 서빙)은 이런 헤더가 **전혀 없습니다** — `curl -I https://paceleague.co.kr/login.html`로 재현 확인.

`paceleague.co.kr`에는 로그인(`login.html`)과 투표/글쓰기 버튼이 있는 커뮤니티 피드(`index.html`, `post.html`)가 있으므로, `X-Frame-Options`/`frame-ancestors` CSP가 없으면 공격자가 이 사이트를 투명 iframe으로 자기 페이지에 얹어 사용자가 모르게 로그인/투표/글쓰기 버튼을 클릭하게 유도하는 **클릭재킹**이 가능합니다.

**2026-08-10 조치 완료.** 이 저장소가 관리하지 않는 서버 측 Nginx 설정(`/etc/nginx/conf.d/paceleague.conf`)이라 SSM(`AWS-RunShellScript`)으로 EC2에 직접 적용했습니다 — `paceleague.co.kr`/`www.paceleague.co.kr` 서버 블록(443)에 아래 헤더를 추가:
```
add_header X-Frame-Options "DENY" always;
add_header X-Content-Type-Options "nosniff" always;
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
```
적용 전 기존 설정 파일을 `paceleague.conf.bak.<타임스탬프>`로 백업하고, `nginx -t` 문법 검증 통과 후에만 `systemctl reload nginx`가 실행되도록 스크립트로 처리(검증 실패 시 자동 롤백). `curl -I https://paceleague.co.kr/login.html`로 헤더 적용과 `api.paceleague.co.kr` 정상 동작을 함께 재확인했습니다.

이 저장소의 배포 파이프라인(`.github/workflows/deploy.yml`, `.github/ssm-commands.json`)은 건드리지 않았습니다 — Nginx 설정은 여전히 이 저장소 밖(서버)에서만 관리되며, 이번 변경도 저장소에 기록되지 않는 서버 측 상태이므로 향후 서버 재구축/AMI 교체 시에는 이 문서를 참고해 다시 적용해야 합니다.

### 배포 시 디스크 부족으로 이미지가 갱신되지 않는 문제

`paceleague` 인스턴스의 루트 디스크는 8GB뿐이고, 배포마다 이전 이미지가 `<none>` 태그로 남아 쌓입니다(이미지당 ~390MB). `.github/ssm-commands.json`은 원래 이걸 정리하지 않아서, 2026-07-12에 디스크가 99%까지 차서 `docker pull`이 새 레이어(`app.jar`)를 받다가 `no space left on device`로 실패한 적이 있습니다 — GitHub Actions의 빌드/푸시는 성공했는데도 EC2에는 이전 이미지가 그대로 떠 있어서, 배포가 "성공"으로 보이지만 실제로는 반영되지 않는 상태였습니다(`docker pull`이 실패해도 스크립트가 멈추지 않고 기존 로컬 이미지로 계속 실행됨). 재발 방지로 `ssm-commands.json` 마지막에 `docker image prune -f`/`docker builder prune -f`를 추가했습니다. 만약 이후에도 배포했는데 실제 동작이 코드 변경과 다르다면, 가장 먼저 EC2 디스크 사용량(`df -h`)과 `docker pull` 실패 여부부터 의심할 것.

### 검색엔진 크롤링 차단 (`api.paceleague.co.kr`)

`api.paceleague.co.kr`은 API 전용 도메인이라 검색엔진에 노출될 필요가 없습니다. `api/src/main/resources/static/robots.txt`(Spring Boot 기본 정적 리소스 서빙)로 전체 `Disallow: /`를 응답하며, `SecurityConfig`의 공개 경로 목록에 `/robots.txt`를 추가해 인증 없이 200으로 받을 수 있게 했습니다 — robots.txt가 401/403 등 4xx로 응답되면 대부분의 크롤러가 "제약 없음"으로 해석해 오히려 전체 크롤링을 허용해버리기 때문에, 반드시 인증 예외 목록에 넣어야 의도대로 동작합니다.

`paceleague.co.kr`(정적 사이트, `web/`)은 이미 `sitemap.xml`을 두고 있는 의도적인 인덱싱 대상이라, 2026-08-20 `web/robots.txt`(`User-agent: * / Allow: / / Sitemap: https://paceleague.co.kr/sitemap.xml`)를 추가해 전체 허용과 sitemap 위치를 명시적으로 선언했습니다 — 그 전까지는 파일이 아예 없어 크롤러 기본값(허용)에 암묵적으로만 의존했습니다. 이 크롤링 차단은 `api.paceleague.co.kr`에만 적용됩니다.

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

즉 **`main`에 push 한 번으로 `api.paceleague.co.kr`(Java 앱)과 `paceleague.co.kr`(정적 사이트)이 동시에 갱신**됩니다. (2026-08-11 갱신: 아래 미디어 첨부 기능을 위해 S3 버킷 `paceleague-media`가 처음으로 추가됐습니다 — 그 전까지는 새 AWS 리소스 없이 기존 EC2/ECR 구조만 썼습니다.)

## AWS 자격증명 관련 참고

배포용 IAM 사용자(`github-actions-deploy`)는 ECR push, SSM 명령 실행 등 **배포에 필요한 권한으로 좁게 스코프**되어 있습니다. `ec2:DescribeInstances`, `route53:ListHostedZones` 등 조회 권한은 없어서, 이 문서의 인프라 정보는 SSM(`AWS-RunShellScript`)으로 EC2 내부에서 직접 조회해 확인한 것입니다. EC2 인스턴스 목록/Route53 레코드 등을 다시 확인하려면 별도의 조회 권한이 있는 IAM 사용자나 AWS 콘솔이 필요합니다.

`iam:*`/`s3:CreateBucket`/`s3:PutBucketPolicy`/`s3:PutBucketCORS`도 원래 스코프 밖이었지만, 2026-08-11 실제로 시도해보니 `s3:*`(버킷 생성/정책/CORS 설정까지)는 되고 `iam:PutRolePolicy`만 막혀 있는 것으로 확인됐습니다(아래 섹션 참고) — 이 사용자의 실제 권한 경계가 문서화된 것보다 넓다는 뜻이므로, 향후 세션에서도 "문서에 없는 권한 = 무조건 안 됨"으로 단정하지 말고 읽기 전용 명령으로 먼저 확인할 것.

## 미디어(S3 + Rekognition) 인프라 — 2026-08-11 추가, 프로비저닝 완료

게시글 이미지/동영상/링크 첨부 기능(`api/.../media` 도메인, [architecture.md](./architecture.md#게시글-미디어-첨부이미지동영상링크--2026-08-11-추가))을 위해 아래를 프로비저닝했습니다.

- S3 버킷 `paceleague-media` 생성 (리전 `ap-northeast-2`)
- 퍼블릭 액세스 블록: ACL은 차단, 버킷 정책(`BlockPublicPolicy`)은 허용 — 아래 버킷 정책으로만 공개 범위를 좁게 열기 위함
- 버킷 정책: `media/*` prefix만 `s3:GetObject` 공개 허용(그 외 경로/버킷 전체는 비공개) — 실제로 `media/` 밖 경로는 403, 안쪽은 200으로 재현 확인함
- 버킷 CORS: `https://paceleague.co.kr`, `https://www.paceleague.co.kr`, `http://localhost:*` 오리진에서 `PUT`/`GET` 허용(브라우저가 presigned URL로 직접 업로드할 수 있도록) — 이건 Spring `CorsConfig`와 완전히 별개의, S3 버킷 자체의 CORS 설정입니다.
- EC2 인스턴스 프로필 역할 `paceleague-s3-read`(원래 `AwsTranslateConfig`가 Translate 호출용으로 쓰던 역할)에 아래 인라인 정책 추가 — `github-actions-deploy`에는 `iam:PutRolePolicy` 권한이 없어 처음엔 실패했고(2026-08-10 보안조치 때처럼 이 IAM 사용자의 권한 밖 작업), 사용자가 AWS 콘솔에서 직접 역할에 인라인 정책을 추가한 뒤 **EC2 인스턴스 자체 자격증명으로 `s3:PutObject`/`HeadObject`/`DeleteObject`와 `rekognition:DetectModerationLabels` 호출을 실제로 성공시켜 반영을 확인**했습니다(SSM으로 EC2에서 직접 `aws s3api put-object`/`aws rekognition detect-moderation-labels` 실행, Rekognition은 진짜 이미지가 아니라 `InvalidImageFormatException`이 났는데 이건 권한이 있다는 뜻 — `AccessDenied`가 아니므로).

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {"Sid": "MediaS3", "Effect": "Allow",
     "Action": ["s3:PutObject", "s3:GetObject", "s3:DeleteObject"],
     "Resource": "arn:aws:s3:::paceleague-media/media/*"},
    {"Sid": "MediaRekognition", "Effect": "Allow",
     "Action": ["rekognition:DetectModerationLabels", "rekognition:StartContentModeration", "rekognition:GetContentModeration"],
     "Resource": "*"}
  ]
}
```

이 정책이 붙기 전까지는 EC2에서 실행 중인 앱이 S3 업로드 완료 처리(`/api/media/{id}/complete`)나 Rekognition 모더레이션 호출 시 `AccessDenied`로 실패합니다 — 코드 배포 자체는 문제없이 되지만 미디어 첨부 기능만 이 정책이 붙을 때까지 동작하지 않습니다. Rekognition 리전 지원 여부는 `ap-northeast-2`로 확인됨(읽기 전용 API 호출 시 `AccessDeniedException`이 떴을 뿐 엔드포인트 자체는 인식됨 — 리전 미지원이었다면 다른 종류의 에러가 났을 것).
