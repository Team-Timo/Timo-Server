-- 하위 태스크 완료가 날짜별(subtask_completions)로 이관되어 규칙 레벨 subtasks.completed는 미사용이 되었다.
-- ddl-auto: update는 컬럼을 자동 삭제하지 않으므로, 기존 DB에서는 이 SQL로 제거해야
-- 새 하위 태스크 INSERT 시 NOT NULL 위반이 발생하지 않는다.

ALTER TABLE subtasks
    DROP COLUMN completed;
