-- 커뮤니티: "코스추천" 게시판 추가 + 추천/비추천 → 좋아요(비추천 폐지).
-- 스키마 변경 없음(데이터만) — 운영/로컬 모두 이 SQL을 직접 실행해야 합니다(ddl-auto 는 데이터를 넣지 않음).
-- BoardLabelPolicy 에 course slug 번역이 함께 들어갑니다.

-- 1) 코스추천 게시판 (크루 홍보 다음 순서)
INSERT INTO board (slug, name, description, display_order, create_at, update_at)
VALUES ('course', '코스추천', '내가 뛴 러닝 코스를 추천해보세요.', 5, NOW(), NOW());

-- 2) 비추천 폐지 — 기존 비추천(-1) 기록을 지우고 score 를 좋아요 수로 다시 맞춘다.
DELETE FROM post_vote WHERE vote_value < 0;
UPDATE post p SET p.score = (SELECT COUNT(*) FROM post_vote v WHERE v.post_sno = p.sno);

DELETE FROM comment_vote WHERE vote_value < 0;
UPDATE comment c SET c.score = (SELECT COUNT(*) FROM comment_vote v WHERE v.comment_sno = c.sno);
