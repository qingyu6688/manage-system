ALTER TABLE sys_operation_log
    ADD COLUMN log_type VARCHAR(20) NOT NULL DEFAULT 'OPERATION' COMMENT '日志类型：OPERATION、LOGIN' AFTER id,
    ADD KEY idx_operation_log_type_created (log_type, created_at),
    COMMENT = '系统操作与登录日志';

UPDATE sys_menu
SET title = '日志管理'
WHERE id = 60
  AND menu_type = 'MENU';
