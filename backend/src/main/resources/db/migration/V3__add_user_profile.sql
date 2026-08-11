ALTER TABLE sys_user
    ADD COLUMN real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名' AFTER nickname,
    ADD COLUMN address VARCHAR(255) DEFAULT NULL COMMENT '联系地址' AFTER avatar,
    ADD COLUMN bio VARCHAR(500) DEFAULT NULL COMMENT '个人简介' AFTER address;

UPDATE sys_user
SET real_name = nickname
WHERE real_name IS NULL OR real_name = '';

INSERT INTO sys_menu (
    id, parent_id, menu_type, title, route_name, path, component, permission, icon, sort,
    enabled, hidden, keep_alive, hidden_tab, iframe, external_link, badge, text_badge,
    fixed_tab, active_path, full_page
)
VALUES (
    40, 10, 'MENU', '个人中心', 'UserCenter', 'user-center', '/system/user-center', NULL,
    'ri:user-3-line', 40, 1, 1, 1, 1, 0, NULL, 0, NULL, 0, NULL, 0
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT id, 40
FROM sys_role
WHERE code IN ('R_SUPER', 'R_ADMIN') AND deleted = 0;
