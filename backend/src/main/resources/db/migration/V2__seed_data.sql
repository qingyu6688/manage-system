INSERT INTO sys_role (id, name, code, description, enabled)
VALUES
    (1, '超级管理员', 'R_SUPER', '拥有系统全部管理权限', 1),
    (2, '系统管理员', 'R_ADMIN', '负责日常用户管理', 1);

INSERT INTO sys_user (
    id, username, password, nickname, gender, phone, email, avatar, status, created_by, updated_by
)
VALUES
    (1, 'admin', '$2b$10$F55Wnt/hnJ/QHzGsSkDkZeuuOrhzSKJLgnc.9WaIMFIDPZKvV.avy', '超级管理员', 1,
     '13800000000', 'admin@example.com', '', 1, 'system', 'system'),
    (2, 'operator', '$2b$10$F55Wnt/hnJ/QHzGsSkDkZeuuOrhzSKJLgnc.9WaIMFIDPZKvV.avy', '系统管理员', 2,
     '13900000000', 'operator@example.com', '', 1, 'system', 'system');

INSERT INTO sys_menu (
    id, parent_id, menu_type, title, route_name, path, component, permission, icon, sort,
    enabled, hidden, keep_alive, hidden_tab, iframe, external_link, badge, text_badge,
    fixed_tab, active_path, full_page
)
VALUES
    (1, 0, 'MENU', '工作台', 'Dashboard', '/dashboard', '/index/index', NULL, 'ri:pie-chart-line', 10,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (2, 1, 'MENU', '控制台', 'Console', 'console', '/dashboard/console', NULL, 'ri:home-smile-2-line', 10,
     1, 0, 0, 0, 0, NULL, 0, NULL, 1, NULL, 0),
    (10, 0, 'MENU', '系统管理', 'System', '/system', '/index/index', NULL, 'ri:settings-3-line', 20,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (11, 10, 'MENU', '用户管理', 'User', 'user', '/system/user', NULL, 'ri:user-line', 10,
     1, 0, 1, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (12, 11, 'BUTTON', '新增用户', NULL, NULL, NULL, 'add', NULL, 10,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (13, 11, 'BUTTON', '编辑用户', NULL, NULL, NULL, 'edit', NULL, 20,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (14, 11, 'BUTTON', '注销用户', NULL, NULL, NULL, 'delete', NULL, 30,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (20, 10, 'MENU', '角色管理', 'Role', 'role', '/system/role', NULL, 'ri:user-settings-line', 20,
     1, 0, 1, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (21, 20, 'BUTTON', '新增角色', NULL, NULL, NULL, 'add', NULL, 10,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (22, 20, 'BUTTON', '编辑角色', NULL, NULL, NULL, 'edit', NULL, 20,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (23, 20, 'BUTTON', '删除角色', NULL, NULL, NULL, 'delete', NULL, 30,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (24, 20, 'BUTTON', '菜单授权', NULL, NULL, NULL, 'permission', NULL, 40,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (30, 10, 'MENU', '菜单管理', 'Menus', 'menu', '/system/menu', NULL, 'ri:menu-line', 30,
     1, 0, 1, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (31, 30, 'BUTTON', '新增菜单', NULL, NULL, NULL, 'add', NULL, 10,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (32, 30, 'BUTTON', '编辑菜单', NULL, NULL, NULL, 'edit', NULL, 20,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0),
    (33, 30, 'BUTTON', '删除菜单', NULL, NULL, NULL, 'delete', NULL, 30,
     1, 0, 0, 0, 0, NULL, 0, NULL, 0, NULL, 0);

INSERT INTO sys_user_role (user_id, role_id)
VALUES (1, 1), (2, 2);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE deleted = 0;

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES
    (2, 1), (2, 2),
    (2, 10), (2, 11), (2, 12), (2, 13), (2, 14);
