-- 태그 소유자(user_id)와 기본 태그 여부(is_default) 컬럼 추가.
-- 운영은 ddl-auto: validate 이므로 배포 전에 반드시 먼저 실행해야 한다.

ALTER TABLE tags
    ADD COLUMN user_id BIGINT NULL,
    ADD COLUMN is_default BIT(1) NOT NULL DEFAULT b'0';

ALTER TABLE tags
    ADD CONSTRAINT uk_tags_user_id_name UNIQUE (user_id, name);

ALTER TABLE tags
    ADD CONSTRAINT fk_tags_user FOREIGN KEY (user_id) REFERENCES users (id);

-- 모든 사용자가 공유하는 기본 태그. 소유자가 없으므로 user_id는 NULL이다.
INSERT INTO tags (user_id, name, is_default)
VALUES (NULL, '일상', b'1'),
       (NULL, '운동', b'1'),
       (NULL, '업무', b'1'),
       (NULL, '과제', b'1');
