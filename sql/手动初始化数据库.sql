CREATE DATABASE IF NOT EXISTS manage_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE manage_system;

-- 表结构与初始数据由 Flyway 迁移维护。
-- 手动初始化时，请按顺序执行：
-- 1. backend/src/main/resources/db/migration/V1__init_schema.sql
-- 2. backend/src/main/resources/db/migration/V2__seed_data.sql
