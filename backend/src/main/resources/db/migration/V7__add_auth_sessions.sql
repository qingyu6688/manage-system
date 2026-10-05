ALTER TABLE sys_user ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0 COMMENT '会话撤销版本';

CREATE TABLE sys_auth_session (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    refresh_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_auth_session_user (user_id),
    KEY idx_auth_session_expiry (expires_at),
    CONSTRAINT fk_auth_session_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);
