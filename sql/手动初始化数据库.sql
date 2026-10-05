CREATE DATABASE IF NOT EXISTS manage_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 只需要创建空库。表结构和初始数据由后端启动时的 Flyway 按版本自动执行，
-- 不要手动执行 backend/src/main/resources/db/migration/ 下的迁移文件：
-- 手动建表后库里没有 flyway_schema_history，后端启动会因“非空库缺少迁移历史”而拒绝启动。
