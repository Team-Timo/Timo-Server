-- AI 유사 title 히스토리 조회(AiTodoQueryRepository#findActualDurationHistoriesBySimilarTitle)를
-- LIKE 전체 스캔 대신 인덱스 range scan + LIMIT으로 후보를 추린 뒤 애플리케이션에서
-- 부분 문자열 유사도를 판정하도록 리팩토링했다.
-- ddl-auto: update는 이미 존재하는 테이블에 인덱스를 안정적으로 추가하지 않으므로
-- 기존 DB(특히 prod)에는 아래 SQL을 수동 적용해야 한다.

ALTER TABLE timer_records
    ADD INDEX idx_timer_records_user_ended (user_id, ended_at);
