#!/usr/bin/env python3
"""
PaceLeague 게시판에 운영 계정으로 원본 글을 올리는 재사용 도구.

콘텐츠는 여기서 만들지 않는다 — posts/*.json 파일에 미리 작성된
{title, content} 목록을 읽어서 API로 등록만 한다. 새 글을 올리고 싶으면
posts/ 아래에 새 JSON 파일을 추가하고 다시 실행하면 된다.

사용법:
    python post_to_board.py posts/2026-09-17-running-tips.json
    python post_to_board.py posts/2026-09-17-running-tips.json --dry-run
    python post_to_board.py posts/2026-09-17-running-tips.json --board qna --delay 10

환경변수 (같은 폴더의 .env 파일에서도 읽음 — .gitignore 처리되어 있음):
    PACELEAGUE_API_BASE       기본값 https://api.paceleague.co.kr
    PACELEAGUE_POSTER_ID      게시자 계정 아이디
    PACELEAGUE_POSTER_PASSWORD 게시자 계정 비밀번호

중복 게시 방지: 파일마다 posts/.posted/<파일명>.json 에 이미 올린 글의
인덱스를 기록해두고, 다음 실행 때 건너뛴다. 같은 파일을 두 번 실행해도
안전하다.
"""
import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
POSTED_STATE_DIR = SCRIPT_DIR / "posts" / ".posted"


def load_dotenv(path: Path) -> None:
    if not path.exists():
        return
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        os.environ.setdefault(key.strip(), value.strip())


def api_request(base_url: str, path: str, method: str = "GET", body: dict | None = None, token: str | None = None) -> dict:
    url = base_url.rstrip("/") + path
    data = json.dumps(body, ensure_ascii=False).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json; charset=utf-8")
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        payload = e.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"{method} {path} -> HTTP {e.code}: {payload}") from None


def login(base_url: str, member_id: str, password: str) -> str:
    result = api_request(base_url, "/api/member/login", "POST", {"memberId": member_id, "password": password})
    if not result.get("success"):
        raise RuntimeError(f"로그인 실패: {result}")
    return result["data"]["accessToken"]


def resolve_board_sno(base_url: str, slug: str) -> int:
    result = api_request(base_url, "/api/board?lang=ko", "GET")
    if not result.get("success"):
        raise RuntimeError(f"보드 목록 조회 실패: {result}")
    for board in result["data"]:
        if board["slug"] == slug:
            return board["sno"]
    available = ", ".join(b["slug"] for b in result["data"])
    raise RuntimeError(f"'{slug}' 보드를 찾을 수 없습니다. 사용 가능한 슬러그: {available}")


def load_posted_state(state_file: Path) -> set:
    if not state_file.exists():
        return set()
    return set(json.loads(state_file.read_text(encoding="utf-8")))


def save_posted_state(state_file: Path, posted: set) -> None:
    state_file.parent.mkdir(parents=True, exist_ok=True)
    state_file.write_text(json.dumps(sorted(posted)), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("posts_file", help="posts/*.json 경로 (형식: [{\"title\": ..., \"content\": ...}, ...])")
    parser.add_argument("--board", default=os.environ.get("PACELEAGUE_BOARD_SLUG", "free"), help="게시할 보드 슬러그 (기본: free)")
    parser.add_argument("--delay", type=float, default=8.0, help="글 사이 대기 시간(초), 너무 빠른 연속 게시로 보이지 않게 (기본: 8)")
    parser.add_argument("--dry-run", action="store_true", help="실제로 올리지 않고 계획만 출력")
    args = parser.parse_args()

    load_dotenv(SCRIPT_DIR / ".env")

    base_url = os.environ.get("PACELEAGUE_API_BASE", "https://api.paceleague.co.kr")
    member_id = os.environ.get("PACELEAGUE_POSTER_ID")
    password = os.environ.get("PACELEAGUE_POSTER_PASSWORD")

    if not args.dry_run and not (member_id and password):
        print("PACELEAGUE_POSTER_ID / PACELEAGUE_POSTER_PASSWORD가 설정되어 있지 않습니다 "
              "(scripts/board-seed/.env 파일을 만들거나 환경변수로 지정하세요).", file=sys.stderr)
        return 1

    posts_path = Path(args.posts_file)
    if not posts_path.is_absolute():
        posts_path = Path.cwd() / posts_path
    posts = json.loads(posts_path.read_text(encoding="utf-8"))

    state_file = POSTED_STATE_DIR / f"{posts_path.stem}.json"
    posted_indices = load_posted_state(state_file)

    pending = [(i, p) for i, p in enumerate(posts) if i not in posted_indices]
    if not pending:
        print(f"{posts_path.name}: 이미 전부 게시됨 ({len(posts)}개). 새 글을 올리려면 새 JSON 파일을 만드세요.")
        return 0

    print(f"{posts_path.name}: 총 {len(posts)}개 중 {len(pending)}개 게시 예정 (보드: {args.board})")

    if args.dry_run:
        for i, p in pending:
            print(f"  [{i}] {p['title']}")
        print("(--dry-run 이라 실제로 올리지 않았습니다)")
        return 0

    token = login(base_url, member_id, password)
    board_sno = resolve_board_sno(base_url, args.board)

    for i, p in pending:
        try:
            result = api_request(
                base_url, f"/api/board/{board_sno}/posts", "POST",
                {"title": p["title"], "content": p["content"], "recordSno": None, "attachmentMediaIds": []},
                token=token,
            )
            if not result.get("success"):
                raise RuntimeError(str(result))
            post_sno = result["data"]["sno"]
            print(f"  [{i}] 게시 완료 (postSno={post_sno}): {p['title']}")
            posted_indices.add(i)
            save_posted_state(state_file, posted_indices)
        except Exception as e:
            print(f"  [{i}] 실패: {p['title']} -> {e}", file=sys.stderr)
            break  # 하나 실패하면 중단 — 원인 확인 후 재실행하면 이미 올라간 건 건너뛴다

        if (i, p) != pending[-1]:
            time.sleep(args.delay)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
