CREATE TABLE sys_announcement (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    title VARCHAR(150) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告正文',
    type VARCHAR(20) NOT NULL COMMENT '类型：SYSTEM、ACTIVITY、REMINDER',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT、PUBLISHED',
    is_top TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
    published_by VARCHAR(30) DEFAULT NULL COMMENT '发布人',
    published_at DATETIME DEFAULT NULL COMMENT '发布时间',
    created_by VARCHAR(30) NOT NULL COMMENT '创建人',
    updated_by VARCHAR(30) NOT NULL COMMENT '更新人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    KEY idx_announcement_status_top (status, is_top, published_at),
    KEY idx_announcement_type (type),
    KEY idx_announcement_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告';

INSERT INTO sys_menu (
    id, parent_id, menu_type, title, route_name, path, component, permission, icon, sort,
    enabled, hidden, keep_alive, hidden_tab, iframe, external_link, badge, text_badge,
    fixed_tab, active_path, full_page
)
VALUES
    (50, 10, 'MENU', '公告管理', 'Announcement', 'announcement', '/system/announcement', NULL,
     'ri:notification-3-line', 40, 1, 0, 1, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (51, 50, 'BUTTON', '新增公告', NULL, NULL, NULL, 'add', NULL, 10,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (52, 50, 'BUTTON', '编辑公告', NULL, NULL, NULL, 'edit', NULL, 20,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (53, 50, 'BUTTON', '删除公告', NULL, NULL, NULL, 'delete', NULL, 30,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (54, 50, 'BUTTON', '发布公告', NULL, NULL, NULL, 'publish', NULL, 40,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM sys_role role
CROSS JOIN sys_menu menu
WHERE role.code IN ('R_SUPER', 'R_ADMIN')
  AND role.deleted = 0
  AND menu.id BETWEEN 50 AND 54;

INSERT INTO sys_announcement (
    title, content, type, status, is_top, published_by, published_at,
    created_by, updated_by
)
VALUES (
    '欢迎使用通用后台管理系统',
    '公告管理模块已经启用，管理员可以在此维护系统通知、活动公告和提醒信息。',
    'SYSTEM', 'PUBLISHED', 1, 'system', CURRENT_TIMESTAMP, 'system', 'system'
);
