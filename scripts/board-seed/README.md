# 게시판 콘텐츠 시딩 도구

커뮤니티 게시판이 비어 보이지 않도록, **운영 계정으로 원본 글**을 올리는 재사용 가능한 도구.
다른 사이트 글을 긁어오는 스크래핑 봇이 아니다 — 콘텐츠는 항상 사람이 미리 작성/검수한 뒤
`posts/*.json`에 넣어두고, 이 스크립트는 그걸 API로 등록하는 역할만 한다.

## 운영 계정

- 아이디: `paceleague_official`, 닉네임: "페이스리그 운영팀" (2026-09-17 생성)
- 일반 `member` 계정이며 관리자(`admin`) 계정과는 무관 — 게시판 글쓰기는 member 로그인만 가능하기 때문.
- 비밀번호는 커밋하지 않는다. `.env`에 저장하고 `.gitignore`(`*.env` 아니라 `.env` 패턴)로 이미 제외됨.

## 사용법

```bash
cd scripts/board-seed
cp .env.example .env   # 처음 한 번만, 실제 비밀번호로 채우기
python post_to_board.py posts/2026-09-17-running-tips.json --dry-run   # 미리보기
python post_to_board.py posts/2026-09-17-running-tips.json            # 실제 게시
```

Windows에서 한글이 콘솔에 깨져 보이면(실제 게시 내용과는 무관, 표시 문제일 뿐) `PYTHONIOENCODING=utf-8 python post_to_board.py ...`로 실행하세요.

- `--board <slug>`: 게시할 보드 (기본 `free`, 다른 값은 `qna`/`verify`/`crew_promo`)
- `--delay <초>`: 글 사이 대기 시간 (기본 8초 — 너무 빠르게 연속으로 올리면 부자연스러움)
- 같은 파일을 다시 실행해도 안전함: `posts/.posted/<파일명>.json`에 이미 올라간 인덱스를 기록해두고 건너뜀.
- 하나라도 실패하면 그 자리에서 멈춤 (원인 확인 후 재실행하면 이미 올라간 글은 다시 안 올라감).

## 새 글 배치 추가하는 법

1. `posts/` 밑에 새 JSON 파일 생성 (예: `posts/2026-10-01-batch2.json`), 형식:
   ```json
   [
     { "title": "제목", "content": "<p>본문. 기본 HTML(p, br, b, strong, i, em, a, img, video)만 허용됨.</p>" }
   ]
   ```
2. 콘텐츠는 **직접 쓰거나, Claude에게 원본 글 초안을 요청**해서 검수 후 넣는다 — 다른 사이트 글 복사는 안 됨
   (저작권 문제 + 애드센스 "스크랩 콘텐츠" 정책 위반 소지, `docs/infra.md` 2026-09-17 애드센스 섹션 참고).
3. `python post_to_board.py posts/새파일.json --dry-run`으로 먼저 확인.

## 주의사항

- **자동 반복 실행(cron 등)으로 만들지 않았다** — 매번 사람이 만든 콘텐츠를 확인하고 수동으로 실행하는 걸 의도함.
  완전 자동화하면 "이것도 봇이 자동 생성한 콘텐츠 아니냐"는 의심을 다시 살 수 있음.
- 여러 언어로 무작위로 올리지 않는다 — 이 계정은 "운영팀 공지" 성격이라 한국어 위주로, 필요하면 명시적으로 다국어 버전을 각각 작성.
- `docs/withdraw-and-moderation-plan.md`의 게시판 신고 자동숨김 로직은 이 계정에도 동일하게 적용됨(신고 누적 시 숨겨짐) — 정상적인 운영 콘텐츠라면 문제 없음.
