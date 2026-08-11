CREATE TABLE sys_operation_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    username VARCHAR(30) NOT NULL COMMENT '操作账号',
    method VARCHAR(10) NOT NULL COMMENT 'HTTP方法',
    path VARCHAR(300) NOT NULL COMMENT '接口路径',
    query_params VARCHAR(1000) DEFAULT NULL COMMENT '查询参数',
    status_code INT NOT NULL COMMENT 'HTTP状态码',
    success TINYINT(1) NOT NULL COMMENT '是否成功',
    ip_address VARCHAR(64) DEFAULT NULL COMMENT '客户端IP',
    user_agent VARCHAR(500) DEFAULT NULL COMMENT '客户端标识',
    duration_ms BIGINT NOT NULL COMMENT '执行耗时毫秒',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_operation_log_created_at (created_at),
    KEY idx_operation_log_username (username),
    KEY idx_operation_log_method_success (method, success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志';

CREATE TABLE sys_file (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    storage_name VARCHAR(100) NOT NULL COMMENT '随机存储名',
    content_type VARCHAR(150) NOT NULL COMMENT '内容类型',
    file_size BIGINT UNSIGNED NOT NULL COMMENT '文件大小字节',
    uploader VARCHAR(30) NOT NULL COMMENT '上传人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_file_storage_name (storage_name),
    KEY idx_sys_file_original_name (original_name),
    KEY idx_sys_file_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件元数据';

CREATE TABLE sys_message (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    title VARCHAR(150) NOT NULL COMMENT '消息标题',
    content TEXT NOT NULL COMMENT '消息正文',
    type VARCHAR(20) NOT NULL COMMENT '类型：SYSTEM、REMINDER、TASK',
    sender VARCHAR(30) NOT NULL COMMENT '发送人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    KEY idx_sys_message_type_created (type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息正文';

CREATE TABLE sys_message_receiver (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '接收记录ID',
    message_id BIGINT UNSIGNED NOT NULL COMMENT '消息ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '接收用户ID',
    read_at DATETIME DEFAULT NULL COMMENT '已读时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_message_receiver (message_id, user_id),
    KEY idx_message_receiver_user_read (user_id, read_at),
    CONSTRAINT fk_message_receiver_message FOREIGN KEY (message_id) REFERENCES sys_message (id),
    CONSTRAINT fk_message_receiver_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息接收状态';

INSERT INTO sys_menu (
    id, parent_id, menu_type, title, route_name, path, component, permission, icon, sort,
    enabled, hidden, keep_alive, hidden_tab, iframe, external_link, badge, text_badge,
    fixed_tab, active_path, full_page
)
VALUES
    (60, 10, 'MENU', '操作日志', 'OperationLog', 'log', '/system/log', NULL,
     'ri:file-list-3-line', 50, 1, 0, 1, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (61, 60, 'BUTTON', '删除日志', NULL, NULL, NULL, 'delete', NULL, 10,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (62, 60, 'BUTTON', '清空日志', NULL, NULL, NULL, 'clear', NULL, 20,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (70, 10, 'MENU', '文件管理', 'FileManage', 'file', '/system/file', NULL,
     'ri:folder-3-line', 60, 1, 0, 1, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (71, 70, 'BUTTON', '上传文件', NULL, NULL, NULL, 'upload', NULL, 10,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (72, 70, 'BUTTON', '下载文件', NULL, NULL, NULL, 'download', NULL, 20,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (73, 70, 'BUTTON', '删除文件', NULL, NULL, NULL, 'delete', NULL, 30,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (80, 10, 'MENU', '消息通知', 'MessageCenter', 'message', '/system/message', NULL,
     'ri:mail-unread-line', 70, 1, 0, 1, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (81, 80, 'BUTTON', '发送消息', NULL, NULL, NULL, 'send', NULL, 10,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (82, 80, 'BUTTON', '标记已读', NULL, NULL, NULL, 'read', NULL, 20,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM sys_role role
CROSS JOIN sys_menu menu
WHERE role.code = 'R_SUPER'
  AND role.deleted = 0
  AND menu.id IN (60, 61, 62, 70, 71, 72, 73, 80, 81, 82);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM sys_role role
CROSS JOIN sys_menu menu
WHERE role.code = 'R_ADMIN'
  AND role.deleted = 0
  AND menu.id IN (70, 71, 72, 73, 80, 81, 82);

INSERT INTO sys_message (id, title, content, type, sender)
VALUES (
    1,
    '消息通知功能已启用',
    '您可以在消息中心查看通知、标记已读，管理员还可以向指定用户或全部用户发送消息。',
    'SYSTEM',
    'system'
);

INSERT INTO sys_message_receiver (message_id, user_id)
SELECT 1, id FROM sys_user WHERE status = 1 AND deleted = 0;
