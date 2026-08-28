-- 커뮤니티에 "크루 홍보" 게시판 추가.
-- board 테이블 스키마 변경은 없고 시드 행 1개만 추가하므로, 운영/로컬 모두 이 INSERT를 직접 실행해야 합니다
-- (ddl-auto: update 는 데이터를 넣지 않음).
-- BoardLabelPolicy 에 crew_promo slug 번역이 함께 들어갑니다.

INSERT INTO board (slug, name, description, display_order, create_at, update_at)
VALUES ('crew_promo', '크루 홍보', '우리 크루를 소개하고 크루원을 모집하세요.', 4, NOW(), NOW());
