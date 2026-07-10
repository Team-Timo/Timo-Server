-- 태그 소유자(user_id)와 기본 태그 여부(is_default) 컬럼 추가.
-- 운영은 ddl-auto: validate 이므로 배포 전에 반드시 먼저 실행해야 한다.

-- 모든 사용자가 공유하는 기본 태그. 소유자가 없으므로 user_id는 NULL이다.
INSERT INTO tags (user_id, name, is_default)
VALUES (NULL, '일상', b'1'),
       (NULL, '운동', b'1'),
       (NULL, '업무', b'1'),
       (NULL, '과제', b'1');
