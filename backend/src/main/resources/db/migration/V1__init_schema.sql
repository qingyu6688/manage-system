CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(30) NOT NULL COMMENT '登录名',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt密码',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    gender TINYINT NOT NULL DEFAULT 1 COMMENT '性别：1男，2女',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，2停用',
    created_by VARCHAR(30) NOT NULL DEFAULT 'system' COMMENT '创建人',
    updated_by VARCHAR(30) NOT NULL DEFAULT 'system' COMMENT '更新人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_status (status),
    KEY idx_sys_user_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code),
    KEY idx_sys_role_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色';

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '上级节点ID，0表示根节点',
    menu_type VARCHAR(20) NOT NULL DEFAULT 'MENU' COMMENT '类型：MENU或BUTTON',
    title VARCHAR(100) NOT NULL COMMENT '显示名称',
    route_name VARCHAR(100) DEFAULT NULL COMMENT 'Vue路由名称',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由地址',
    component VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    permission VARCHAR(100) DEFAULT NULL COMMENT '按钮权限标识',
    icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    hidden TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否隐藏菜单',
    keep_alive TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否缓存页面',
    hidden_tab TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否隐藏标签页',
    iframe TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否内嵌页面',
    external_link VARCHAR(500) DEFAULT NULL COMMENT '外部链接',
    badge TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否显示徽章',
    text_badge VARCHAR(30) DEFAULT NULL COMMENT '文本徽章',
    fixed_tab TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否固定标签',
    active_path VARCHAR(200) DEFAULT NULL COMMENT '激活菜单路径',
    full_page TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否全屏页面',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_menu_route_name (route_name),
    KEY idx_sys_menu_parent_sort (parent_id, sort),
    KEY idx_sys_menu_permission (permission)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单与按钮权限';

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    KEY idx_sys_user_role_role (role_id),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关系';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    menu_id BIGINT UNSIGNED NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id),
    KEY idx_sys_role_menu_menu (menu_id),
    CONSTRAINT fk_sys_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_sys_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关系';
